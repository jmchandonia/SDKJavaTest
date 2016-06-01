/*
A KBase module: SDKJavaTest

This module should help debug why RBTnSeq crashes in the
development environment
*/

module SDKJavaTest {
     /* version is input a number of seconds to run for */
     typedef structure {
         string ws;
         int num_seconds;
     } VersionInput;

     /* version outputs a report  */
     typedef structure {
         string report_name;
         string report_ref;
     } VersionOutput;

    /* waits, then returns version number of service */
    funcdef version(VersionInput input) returns (VersionOutput output) authentication required;
};
