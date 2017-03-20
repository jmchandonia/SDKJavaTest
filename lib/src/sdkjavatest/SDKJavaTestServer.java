package sdkjavatest;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import us.kbase.auth.AuthToken;
import us.kbase.common.service.JsonServerMethod;
import us.kbase.common.service.JsonServerServlet;
import us.kbase.common.service.JsonServerSyslog;
import us.kbase.common.service.RpcContext;

//BEGIN_HEADER
import java.net.URL;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import us.kbase.common.service.Tuple11;
import us.kbase.common.service.UObject;
import us.kbase.kbasegenomes.Contig;
import us.kbase.kbasegenomes.ContigSet;
import us.kbase.workspace.ObjectIdentity;
import us.kbase.workspace.ObjectSaveData;
import us.kbase.workspace.ProvenanceAction;
import us.kbase.workspace.SaveObjectsParams;
import us.kbase.workspace.WorkspaceClient;
//END_HEADER

/**
 * <p>Original spec-file module name: SDKJavaTest</p>
 * <pre>
 * A KBase module: SDKJavaTest
 * This module should help debug why RBTnSeq crashes in the
 * development environment
 * </pre>
 */
public class SDKJavaTestServer extends JsonServerServlet {
    private static final long serialVersionUID = 1L;
    private static final String version = "0.0.1";
    private static final String gitUrl = "git@github.com:jmchandonia/SDKJavaTest.git";
    private static final String gitCommitHash = "e163d8dc8adcf1f2d8b1a55543a627634c90380d";

    //BEGIN_CLASS_HEADER
    private final String wsUrl;
    private final String shockUrl;
    //END_CLASS_HEADER

    public SDKJavaTestServer() throws Exception {
        super("SDKJavaTest");
        //BEGIN_CONSTRUCTOR
        wsUrl = config.get("workspace-url");
        shockUrl = config.get("shock-url");
        //END_CONSTRUCTOR
    }

    /**
     * <p>Original spec-file function name: version</p>
     * <pre>
     * waits, then returns version number of service
     * </pre>
     * @param   input   instance of type {@link sdkjavatest.VersionInput VersionInput}
     * @return   parameter "output" of type {@link sdkjavatest.VersionOutput VersionOutput}
     */
    @JsonServerMethod(rpc = "SDKJavaTest.version", async=true)
    public VersionOutput version(VersionInput input, AuthToken authPart, RpcContext jsonRpcContext) throws Exception {
        VersionOutput returnVal = null;
        //BEGIN version
        returnVal = SDKJavaTestImpl.run(wsUrl,shockUrl,authPart,input);
        //END version
        return returnVal;
    }
    @JsonServerMethod(rpc = "SDKJavaTest.status")
    public Map<String, Object> status() {
        Map<String, Object> returnVal = null;
        //BEGIN_STATUS
        returnVal = new LinkedHashMap<String, Object>();
        returnVal.put("state", "OK");
        returnVal.put("message", "");
        returnVal.put("version", version);
        returnVal.put("git_url", gitUrl);
        returnVal.put("git_commit_hash", gitCommitHash);
        //END_STATUS
        return returnVal;
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 1) {
            new SDKJavaTestServer().startupServer(Integer.parseInt(args[0]));
        } else if (args.length == 3) {
            JsonServerSyslog.setStaticUseSyslog(false);
            JsonServerSyslog.setStaticMlogFile(args[1] + ".log");
            new SDKJavaTestServer().processRpcCall(new File(args[0]), new File(args[1]), args[2]);
        } else {
            System.out.println("Usage: <program> <server_port>");
            System.out.println("   or: <program> <context_json_file> <output_json_file> <token>");
            return;
        }
    }
}
