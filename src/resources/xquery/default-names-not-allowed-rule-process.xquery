xquery version "3.1";

declare namespace bpmn2 = "http://www.omg.org/spec/BPMN/20100524/MODEL";
declare namespace ifl = "http:///com.sap.ifl.model/Ifl.xsd";


for $process in //(bpmn2:process|bpmn2:subProcess)
return (string($process/@name), string($process/@id))
