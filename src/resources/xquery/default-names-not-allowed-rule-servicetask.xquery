xquery version "3.1";

declare namespace bpmn2 = "http://www.omg.org/spec/BPMN/20100524/MODEL";
declare namespace ifl = "http:///com.sap.ifl.model/Ifl.xsd";

declare function local:valServiceTask($mf, $k as xs:string) as xs:string? {
    $mf/bpmn2:extensionElements/ifl:property[key = $k]/value
};

for $serviceTask in //(bpmn2:serviceTask|bpmn2:exclusiveGateway)
let $activityTypeServiceTask := local:valServiceTask($serviceTask, "activityType")

return (string($serviceTask/@name), string($serviceTask/@id), string($activityTypeServiceTask))