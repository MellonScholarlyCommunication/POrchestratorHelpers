/*

Clone a flow file into N fragments

Required:
  - Attribute `copies` number of copies

Output:
  - `copies` copies flow file
  - `fragment` attribute with the fragment identifiers
*/
import java.util.ArrayList;
import java.util.List;

flowFile = session.get()

if (!flowFile) return

outputRelation = REL_SUCCESS

splitFlowFiles = new ArrayList<>();

try {
    copies = Integer.parseInt(flowFile.getAttribute("copies"))

    for (int i = 0 ; i < copies ; i++) {
       clone = session.clone(flowFile)
       clone = session.putAttribute(clone,"frament", String.valueOf(i))
       splitFlowFiles.add(clone)
    }
}
catch(e) {
  outputRelation = REL_FAILURE
  flowFile = session.putAttribute(flowFile, "error", "PROCESS_ERROR")
  flowFile = session.putAttribute(flowFile, "errorMessage", e.getMessage())
}

session.remove(flowFile)
session.transfer(splitFlowFiles, outputRelation)
