xquery version "3.1";

declare namespace bpmn2 = "http://www.omg.org/spec/BPMN/20100524/MODEL";
declare namespace ifl = "http:///com.sap.ifl.model/Ifl.xsd";

declare function local:valServiceTask($mf as element(bpmn2:serviceTask), $k as xs:string) as xs:string? {
    $mf/bpmn2:extensionElements/ifl:property[key = $k]/value
};

for $serviceTask in /bpmn2:definitions/bpmn2:process/bpmn2:serviceTask
let $activityTypeServiceTask := local:valServiceTask($serviceTask, "activityType")

return (string($serviceTask/@name), string($serviceTask/@id), string($activityTypeServiceTask))