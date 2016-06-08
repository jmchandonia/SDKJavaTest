package sdkjavatest;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.util.zip.*;
import java.net.URL;
import java.text.SimpleDateFormat;

import us.kbase.auth.AuthService;
import us.kbase.auth.AuthToken;
import us.kbase.common.service.*;
import us.kbase.workspace.*;
import us.kbase.shock.client.*;
import us.kbase.kbasegenomes.*;
import us.kbase.kbaseassembly.*;
import us.kbase.kbasereport.*;
import us.kbase.kbasecollections.*;
import us.kbase.common.utils.FastaWriter;

import com.fasterxml.jackson.databind.*;

import org.strbio.IO;
import org.strbio.io.*;
import org.strbio.util.*;

import static java.lang.ProcessBuilder.Redirect;

/**
   This class just runs a simple SDK Java test
*/
public class SDKJavaTestImpl {
    /**
       creates a workspace client; if token is null, client can
       only read public workspaces.
    */
    public static WorkspaceClient createWsClient(String wsURL,
                                                 AuthToken token) throws Exception {
        WorkspaceClient rv = null;

        if (token==null)
            rv = new WorkspaceClient(new URL(wsURL));
        else
            rv = new WorkspaceClient(new URL(wsURL),token);
        rv.setAuthAllowedForHttp(true);
        return rv;
    }

    /**
       helper function to get the reference back when saving an object
    */
    public static String getRefFromObjectInfo(Tuple11<Long, String, String, String, Long, String, Long, String, String, Long, Map<String,String>> info) {
        return info.getE7() + "/" + info.getE1() + "/" + info.getE5();
    }
    
    /**
       store a file in Shock; returns handle.
       If file doesn't exist or can't be read, returns null.
    */
    public static Handle toShock(String shockURL,
                                 AuthToken token,
                                 File f) throws Exception {
        if (!f.canRead())
            return null;
        
        Handle rv = new Handle()
            .withFileName(f.getName())
            .withUrl(shockURL);
        BasicShockClient shockClient = new BasicShockClient(new URL(shockURL), token);
        InputStream is = new BufferedInputStream(new FileInputStream(f));
        ShockNode sn = shockClient.addNode(is,f.getName(),null);
        is.close();
        String shockNodeID = sn.getId().getId();
        rv.setId(shockNodeID);
        return rv;
    }

    /**
       Load a file from Shock; returns File, or null if file couldn't be read.
       If the file from Shock is 0-length, it is deleted and null is returned.
    */
    public static File fromShock(Handle h,
                                 AuthToken token,
                                 File f,
                                 boolean gzip) throws Exception {

        System.err.println("shock cmd equivalent to "+"/usr/bin/curl -k -X GET "+h.getUrl()+" -H \"Authorization: OAuth "+token.toString()+"\""+(gzip ? "| /bin/gzip" : ""));
        
        BasicShockClient shockClient = new BasicShockClient(new URL(h.getUrl()), token);
        ShockNode sn = shockClient.getNode(new ShockNodeId(h.getId()));
        OutputStream os = new FileOutputStream(f);
        if (gzip)
            os = new GZIPOutputStream(os);

        shockClient.getFile(sn,os);

        if (f.length()==0)
            f.delete();

        if (!f.canRead())
            return null;
        
        return f;
    }

    /**
       dumps a (single end) reads object out in fastQ.gz format.  
    */
    public static File dumpSEReadsFASTQ(WorkspaceClient wc,
                                        File tempDir,
                                        String readsRef) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        File fastQFile = File.createTempFile("reads", ".fastq.gz", tempDir);
        fastQFile.delete();

        try {
            us.kbase.kbaseassembly.SingleEndLibrary kasl = wc.getObjects(Arrays.asList(new ObjectIdentity().withRef(readsRef))).get(0).getData().asClassInstance(us.kbase.kbaseassembly.SingleEndLibrary.class);
            Handle h = kasl.getHandle();
            AuthToken token = wc.getToken();

            fastQFile = fromShock(h, token, fastQFile, true);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Error reading single-end library as assembly object");
            // should try to read as KBaseFile.SingleEndLibrary here
            e.printStackTrace();
            // fastQFile.delete(); save for debugging
            fastQFile = null;
        }

        return fastQFile;
    }

    /**
       Make a provenance object
    */
    public static List<ProvenanceAction> makeProvenance(String description,
                                                        String methodName,
                                                        List<UObject> methodParams) throws Exception {
        return new ArrayList<ProvenanceAction>
            (Arrays.asList(new ProvenanceAction()
                           .withDescription(description)
                           .withService("SDKJavaTest")
                           .withServiceVer("1")
                           .withMethod(methodName)
                           .withMethodParams(methodParams)));
    }

    /**
       Make and save Report object, returning its name and reference
    */
    public static String[] makeReport(WorkspaceClient wc,
                                      String ws,
                                      String reportText,
                                      List<String> warnings,
                                      List<WorkspaceObject> objects,
                                      List<ProvenanceAction> provenance) throws Exception {
        String reportName = "rbtnseq_report_"+UUID.randomUUID().toString();
        Report report = new Report()
            .withTextMessage(reportText)
            .withWarnings(warnings)
            .withObjectsCreated(objects);

        ObjectSaveData reportData = new ObjectSaveData()
            .withType("KBaseReport.Report")
            .withData(new UObject(report))
            .withName(reportName)
            .withHidden(1L)
            .withProvenance(provenance);
        String reportRef = getRefFromObjectInfo(wc.saveObjects(new SaveObjectsParams().withWorkspace(ws).withObjects(Arrays.asList(reportData))).get(0));
        return new String[] { reportName, reportRef };
    }
    
    /**
       runs a test
    */
    public static VersionOutput run(String wsURL,
                                    String shockURL,
                                    AuthToken token,
                                    VersionInput input) throws Exception {

        WorkspaceClient wc = createWsClient(wsURL,token);
        File tempDir = new File("/kb/module/work/");

        int numSeconds = (int)input.getNumSeconds().longValue();
        Object timer = new Object();

        // turn local into absolute paths
        String readsRef = input.getInputReadLibrary();
        if (readsRef.indexOf("/") == -1)
            readsRef = input.getWs()+"/"+readsRef;
        
        // for provenance
        String methodName = "SDKJavaTestImpl.run";
        List<UObject> methodParams = Arrays.asList(new UObject(input));
        
        // expected outputs:
        String reportText = "SDK Java Test output:\n";
        List<String> warnings = null;
        List<WorkspaceObject> objects = new ArrayList<WorkspaceObject>();

        try {
            reportText += "\nThis is the SDK Java Test Version 1\n";
            reportText += "\nWaiting "+numSeconds+" seconds.\n";
            synchronized (timer) {
                timer.wait(1000 * numSeconds);
            }
            // dump reads
            File readsFile = dumpSEReadsFASTQ(wc,
                                              tempDir,
                                              readsRef);

            reportText += "\nDumped reads file.\n";
            readsFile.delete();
            reportText += "\nReads file deleted.\n";
        }
        catch (Exception e) {
            reportText += "\n\nERROR: "+e.getMessage();
            warnings = new ArrayList<String>();
            warnings.add("ERROR: "+e.getMessage());
        }

        // generate report with list of objects created
        String[] report = makeReport(wc,
                                     input.getWs(),
                                     reportText,
                                     warnings,
                                     objects,
                                     makeProvenance("SDK Java Test report",
                                                    methodName,
                                                    methodParams));

        VersionOutput rv = new VersionOutput()
            .withReportName(report[0])
            .withReportRef(report[1]);

        return rv;
    }
}
