# Cpilint Custom
This is an extension of the original cpilint application that adds some additional functionalities

## Extra Rules:

### matching-process-direct-channels-required
This rule ensures that for every ProcessDirect receiver channel, there is a sender channel with the same address.
The rule is configured as follows:

<matching-process-direct-channels-required/>

### multi-condition-type-routers-not-allowed
To comply with this rule, an integration flow cannot contain Router steps configured with both XML and non-XML conditions.
The rule is configured as follows:

<multi-condition-type-routers-not-allowed/>

### allowed-receiver-adapters
Using this rule, you can make sure that only specific receiver adapters are used in your integration flows.
The rule can be configured in two different ways. To specify an allow list of receiver adapters, configure the rule as follows:

<allowed-receiver-adapters>
    <allow>odata</allow>
    <allow>soap</allow>
    <allow>rfc</allow>
</allowed-receiver-adapters>

Any receiver adapter not listed is not allowed. To instead specify a deny list of receiver adapters, configure the rule as follows:

<disallowed-receiver-adapters>
    <disallow>odata</disallow>
    <disallow>soap</disallow>
    <disallow>rfc</disallow>
</disallowed-receiver-adapters>

### allowed-scripting-languages
Using this rule, you can specify which scripting languages are allowed in your integration flows and which are not.
The rule can be configured in two different ways. To specify an allow list of scripting languages, configure the rule as follows:

<allowed-scripting-languages>
    <allow>groovy</allow>
</allowed-scripting-languages>
Any scripting language not listed is not allowed. To instead specify a deny list of scripting languages, configure the rule as follows:

<disallowed-scripting-languages>
    <disallow>groovy</disallow>
</disallowed-scripting-languages>

### allowed-sender-adapters
Using this rule, you can make sure that only specific sender adapters are used in your integration flows.

The rule can be configured in two different ways. To specify an allow list of sender adapters, configure the rule as follows:

<allowed-sender-adapters>
    <allow>https</allow>
    <allow>idoc</allow>
    <allow>sftp</allow>
</allowed-sender-adapters>

Any sender adapter not listed is not allowed. To instead specify a deny list of sender adapters, configure the rule as follows:

<disallowed-sender-adapters>
    <disallow>https</disallow>
    <disallow>idoc</disallow>
    <disallow>sftp</disallow>
</disallowed-sender-adapters>

### allowed-xslt-versions
Using this rule, you can make sure that only specific XSLT versions are used in your integration flows.
The rule can be configured in two different ways. To specify an allow list of XSLT versions, configure the rule as follows:

<allowed-xslt-versions>
    <allow>2.0</allow>
    <allow>3.0</allow>
</allowed-xslt-versions>
Any XSLT version not listed is not allowed. To instead specify a deny list of XSLT versions, configure the rule as follows:

<disallowed-xslt-versions>
    <disallow>1.0</disallow>
</disallowed-xslt-versions>

### client-cert-sender-channel-auth-not-allowed
To comply with this rule, an integration flow cannot contain sender channels with client certificates configured directly in the channel.
The rule is configured as follows:

<client-cert-sender-channel-auth-not-allowed/>

### csrf-protection-required
Using this rule, you can ensure that HTTPS sender channels in your integration flows all employ CSRF protection.
The rule is configured as follows:

<csrf-protection-required/>

### allowed-mapping-types
Using this rule, you can make sure that only specific mapping types are used in your integration flows.
The rule can be configured in two different ways. To specify an allow list of mapping types, configure the rule as follows:

<allowed-mapping-types>
    <allow>message-mapping</allow>
</allowed-mapping-types>
Any mapping type not listed is not allowed. To instead specify a deny list of mapping types, configure the rule as follows:

<disallowed-mapping-types>
    <disallow>xslt-mapping</disallow>
</disallowed-mapping-types>

Any mapping type not listed is allowed.

The supported mapping type values are:

message-mapping
operation-mapping
xslt-mapping

### allowed-java-archives
Using this rule, you can specify which Java archives are allowed in your integration flows and which are not.
The rule can be configured in two different ways. To specify an allow list of Java archives, configure the rule as follows:

<allowed-java-archives>
    <allow>jsoup-*.jar</allow>
</allowed-java-archives>
Any Java archive not listed is not allowed. If no Java archive names are specified, no archives are allowed. To instead specify a deny list of Java archives, configure the rule as follows:

<disallowed-java-archives>
    <disallow>jsoup-*.jar</disallow>
</disallowed-java-archives>
Any Java archive not listed is allowed.

Wildcards are supported in archive names. * matches any number of characters (including no characters) and ? matches a single character.

### disallowed-java-archives
Using this rule, you can specify which Java archives are allowed in your integration flows and which are not.

### duplicate-resources-not-allowed
With this rule, you can indicate that your integration flows should not contain duplicate resources, i.e. identical resources that appear in multiple integration flows.
The rule is configured as follows:

<duplicate-resources-not-allowed>
    <resource-type>groovy-script</resource-type>
    <resource-type>javascript-script</resource-type>
</duplicate-resources-not-allowed>
The following resource type values are supported:

message-mapping
xslt-mapping
operation-mapping
javascript-script
groovy-script
java-archive
edmx
wsdl
xml-schema
If you do not specify any resource types, the rule will check all the supported resource types for duplicates.

Please keep in mind that CPILint can only check for duplicates in the iflows you are inspecting. This means that if you only inspect a subset of the iflows in a tenant, and a duplicate resource exists in an iflow that is not in that subset, CPILint will not detect the issue. Example: An iflow in package A contains a Groovy script and an iflow in package B contains a copy of that script. If you only inspect package A, CPILint will not flag an issue because the DuplicateResourcesNotAllowed rule doesn't see the iflow in package B. This behaviour is by design; the user has full control over which iflows will be inspected.

Please also note that the DuplicateResourcesNotAllowed rule doesn't distinguish between resources that are used in the iflow and resources that have been added to the iflow but currently aren't in use. This means that duplicate resources will be flagged by the rule even if they are not currently in use.

### allowed-user-roles
The UserRoles rule lets you specify which user roles should and should not be used in sender channels that support user role authorization.
The rule can be configured in two different ways. To specify an allow list of user roles, configure the rule as follows:

<allowed-user-roles>
    <allow>ExampleRole</allow>
    <allow>AnotherExampleRole</allow>
</allowed-user-roles>
Any user role not listed is not allowed. To instead specify a deny list of user roles, configure the rule as follows:

<disallowed-user-roles>
    <disallow>ExampleRole</disallow>
    <disallow>AnotherExampleRole</disallow>
</disallowed-user-roles>
Any user role not listed is allowed.

### default-names-not-allowed-rule
Since CPI has no concept of comments for each component, we want to make sure that we have meaningful names on components that describe the logic of the iflow, so we check that for all possible CPI components we don't have default names such as "Content Modifier 1", "Content Modifier 2", "Request Reply 1" naming or "groovy1" for filenames

### iflow-matches-name
Defines the rules for the iflow names

### disallowed-scripting-languages
You can specify which programming languages cannot be used

### cleartext-basic-auth-not-allowed
In order to comply with this rule, an integration flow cannot contain receiver channels, that are configured with basic authentication over unencrypted HTTP.
The rule is configured as follows:

<cleartext-basic-auth-not-allowed/>


### unencrypted-data-store-write-not-allowed
With this rule, you can make sure that all data store writes performed in your integration flows are encrypted.

The rule is configured as follows:

<unencrypted-data-store-write-not-allowed/>

### unencrypted-endpoints-not-allowed
In order to comply with this rule, an integration flow cannot contain receiver channels, that are configured with unencrypted HTTP endpoints.
The rule is configured as follows:

<unencrypted-endpoints-not-allowed/>

### csrf-protection-required-with-exclude
Specify which iflows will not need to use csrf protection

### iflow-description-required
With this rule, you can ensure that all your integration flows have a description.
The rule is configured as follows:

<iflow-description-required/>

### unused-parameters-rule
How many times have you defined some external parameters that in the end were not used? CPI provides the "Remove unused parameters" button which would work in a similar fashion as this rule. This rule just asserts that all your defined parameters are being used

### allowed-headers-empty
We have main iflows (reached from outside) and internal iflows communicating via process direct. In both scenarios, the "Allowed headers" setting being empty might be a problem because the headers would get lost between process direct calls if so. In case of main iflows, there are some headers that we allow to receive like the SapAuthenticatedUserName for instance. Right now according to our rule configuration we're only validating on purpose the communications via process direct, not making it mandatory to receive headers on the main iflow but this is configurable on the rule

### response-headers-allowed
During developments, we were faced with an issue where a target system was called and returned an invalid header for CPI. I don't remember the details but if I recall it was because the header exceeds the maximum size that CPI can handle. With this error, we learn not to accept * by default on the response headers of our http calls. This rule is enforcing that

### undeclared-data-type
During developments we realized that we had a property defined on a content modifier without a type specified and for that particular scenario this resulted on a runtime error since CPI assumed that the property was somehow a complex object when we wanted it to be a regular String. So this rule checks all your properties and make sure that for the ones asking for a type (which is not mandatory on cpi), enforces it to be filled in when checking it via this rule

### log-trace-level-enabled-rule
Set the rule if the FER_LogTraceLevelEnabled property must exist in the iflows
The rule is configured as follows:

<log-trace-level-enabled-rule/>

### disallowed-expressions
Specify which regular expressions cannot be used in the iflow and scripts and external parameters.
The rule is configured as follows:

<disallowed-expressions>
	<disallow>\$\{in\.body\}</disallow>
	<disallow>get.*eader</disallow>
</disallowed-expressions>

### disallowed-filter-xpath-property
Specify which regular expression cannot be used inside the Filter object
The rule is configured as follows:

<disallowed-filter-xpath-property>
	<disallow>\/\/records\[1\]</disallow>
	<disallow>\/\/CaseRequest</disallow>
</disallowed-filter-xpath-property>