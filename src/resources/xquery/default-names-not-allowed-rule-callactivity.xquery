xquery version "3.1";

declare namespace bpmn2 = "http://www.omg.org/spec/BPMN/20100524/MODEL";
declare namespace ifl = "http:///com.sap.ifl.model/Ifl.xsd";

declare function local:valCallActivity($mf as element(bpmn2:callActivity), $k as xs:string) as xs:string? {
    $mf/bpmn2:extensionElements/ifl:property[key = $k]/value
};


for $callActivity in //bpmn2:callActivity
let $activityTypeCallActivity := local:valCallActivity($callActivity, "activityType")
let $subActivityTypeCallActivity := local:valCallActivity($callActivity, "subActivityType")

return (string($callActivity/@name), string($callActivity/@id), string($activityTypeCallActivity),string($subActivityTypeCallActivity))