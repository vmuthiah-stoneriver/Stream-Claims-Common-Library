<#--
Input to this template is the Document information. Template is written
to be null safe. 

See OutboundDocumentRenderer.java for how to use this template.

-->
<#setting number_format="###0.#####">

<outboundDocument>
  <document>
    <@element element="agreementId" value="${document.agreementIdDerived!}"/>
    <@element element="agreementTypeCode" value="${document.agreementTypeCodeDerived!}"/>
    <@element element="agreementSubId" value=""/>
    <@element element="agreementSubTypeCode" value="${document.agreementSubTypeCodeDerived!}"/>
    
    <#if (document.createdDateTime)??><#assign createdDateTime = document.createdDateTime?datetime></#if>
    <@dateElement element="createdDateTime" value="${createdDateTime!'null'}"/>
    <@element element="documentName" value="${document.documentName!}"/>    
    <@element element="documentNumber" value="${document.documentNumber!}"/>
    <@element element="documentReferenceNumber" value="${document.documentReferenceNumber!}"/>
    <@element element="documentSourceCode" value="${document.documentSourceCode!}"/>
    <@element element="formatCode" value="${document.formatCode!}"/>
    <@element element="generatedByCode" value="${document.generatedByCode!}"/>
    <@element element="recordId" value="${document.recordId!}"/>
    <@element element="recordSourceCode" value="${document.recordSourceCode!}"/>
    <@element element="recordSourceCodeName" value=""/>
    <@element element="renderedFilePath" value="${document.renderFileName!}"/>
    
    <@element element="deliveryTypeCode" value="${document.deliverTypeCode!}"/>
	<@element element="deliveryDeviceName" value="${deviceName!}"/>
    <@element element="productionGroupCode" value="${document.productionGroupCode!}"/>
    <@element element="paperStockCode" value="${document.paperStockCode!}"/>
    <@element element="contextPackageCode" value="${document.contextPackageCode!}"/>
    <@element element="productionOrderCount" value="${document.productionOrderCount!}"/>
    <@element element="printDuplexInd" value="${document.printDuplex?string('true', 'false')}"/>
    <@element element="multipleRenderedInd" value="${document.multipleRendered?string('true', 'false')}"/>
    <@element element="prePrintedEnclosureInd" value="${document.prePrintedEnclosure?string('true', 'false')}"/>
    
    <@element element="documentTemplateName" value="${document.documentTypeVersionCode.documentTypeVsnTmpltName!}"/>
    
    <documentCompany>
        <@element element="corporationId" value="${document.documentTypeVersionCode.corporationId!}"/>
        <@element element="corporationName" value="${document.documentTypeVersionCode.corporationName!}"/>
        <@element element="companyId" value="${document.documentTypeVersionCode.companyId!}"/>
        <@element element="companyName" value="${document.documentTypeVersionCode.companyName!}"/>
       <#if companyRelatedInfo??>  
        <@element element="companyDBAName" value="${companyRelatedInfo.companyDBAName!}"/>
   	   </#if> 
       <#if companyAddress??>
        <companyMailingAddress>
            <@element element="street1" value="${companyAddress.mailAddressLine1!}"/>
            <@element element="street2" value="${companyAddress.mailAddressLine2!}"/>
            <@element element="city" value="${companyAddress.mailAddressCityName!}"/>
            <@element element="state" value="${companyAddress.mailPostalSvcRegionCode!}"/>
            <@element element="stateName" value="${companyAddress.mailPostalServiceRegionName!}"/>
            <@element element="postalCode" value="${companyAddress.mailPostalCode!}"/>
            <@element element="countryCode" value="${companyAddress.countryCode!}"/>
            <@element element="countryName" value="${companyAddress.countryName!}"/>
        </companyMailingAddress>
       </#if>  
        <#if companyPhoneNumbers??>
          <companyPhoneNumbers>
            <#-- Iterate over phones -->
            <#list companyPhoneNumbers as companyPhone>
              <phone>
                <@element element="phoneNumber" value="${companyPhone.phoneNumber!}"/>
                <@element element="extension" value="${companyPhone.phoneNumberExtension!}"/>
                <@element element="type" value="${companyPhone.usageTypeName!}"/>
              </phone> 
            </#list>           
          </companyPhoneNumbers>
        </#if>
        <@element element="companyEmailAddress" value="${companyEmailAddress.emailAddress!}"/>
        <@element element="companyWebSite" value="${companyWebSite.emailAddress!}"/>
        <#if companyRelatedInfo??>  
          <@element element="companySmallLogoFileName" value="${companyRelatedInfo.companyLogoFileName!}"/>
          <@element element="companyLargeLogoFileName" value="${companyRelatedInfo.companyLogoFileName!}"/>
         </#if> 
    </documentCompany>
  </document>
  <recipient>	
         <#if toRecipient??>
	           <@recipientDetails recipient = toRecipient/>
			   <recipientType>toRecipient</recipientType>
	     </#if>
        <#if ccRecipient??>
           <@recipientDetails recipient = ccRecipient/>
		   <recipientType>ccRecipient</recipientType>
        </#if>
    </recipient>
</outboundDocument>
<#macro recipientDetails recipient>
            <@element element="name" value="${recipient.documentRecipientName!}"/>
			<@element element="recipientRecordId" value="${recipient.recordId!}"/>
            <@element element="partyRecordId" value="${recipient.partyId!}"/>            
            <@element element="deliveryType" value="${recipient.documentRecipientDeliveryTypeCode!}"/>
            <#-- Need:
                - participationContext code / name
                - participationType
                - partyRoleType / TypeName
                - partyType / TypeName
                - phoneNumber, phoneNumberExtension, phoneNumberType
             -->
             <@element element="participationContextCode" value="${recipient.recipientAgreementTypeCode!}"/>
             <@element element="participationContextName" value=""/>
             <@element element="participationType" value="${recipient.recipientParticipantTypeCode!}"/>
             <@element element="participationTypeName" value=""/>
             <@element element="specialRecipientInd" value="${recipient.specialRecipient?string('true', 'false')}"/>
             <@element element="partyRoleType" value=""/>
             <@element element="partyRoleTypeName" value=""/>
             <@element element="partyTypeCode" value=""/>
             <@element element="partyTypeName" value=""/>
             <phone>
                <@element element="phoneNumber" value=""/>
                <@element element="extension" value=""/>
                <@element element="type" value=""/>
            </phone>
            <#if recipient.recipientMailDelivery??>
              <mailingAddress>
                <#-- TODO: countryCode and state not provided by model -->
                <@element element="inCareOfName" value="${recipient.documentRecipientInCareOfName!}"/>
                <@element element="inCareOfRecordId" value="${recipient.inCareOfPartyId!}"/>
                <@element element="attentionToName" value="${recipient.documentRecipientAttentionToName!}"/>
                <@element element="street1" value="${recipient.recipientMailDelivery.documentRecipientMailAddressLine1!}"/>
                <@element element="street2" value="${recipient.recipientMailDelivery.documentRecipientMailAddressLine2!}"/>
                <@element element="city" value="${recipient.recipientMailDelivery.documentRecipientMailAddressCityName!}"/>
                <@element element="state" value="${recipient.recipientMailDelivery.documentRecipientPostalSvcRegionCode!}"/>
                <@element element="stateName" value="${recipient.recipientMailDelivery.documentRecipientPostalSvcRegionName!}"/>
                <@element element="postalCode" value="${recipient.recipientMailDelivery.documentRecipientMailAddressPostalCode!}"/>
                <@element element="countryCode" value="${recipient.recipientMailDelivery.documentRecipientCountryCode!}"/>
                <@element element="countryName" value="${recipient.recipientMailDelivery.documentRecipientCountryName!}"/>
              </mailingAddress>
            </#if>
            <#if recipient.recipientFaxDelivery??>
              <faxInfo>  
                <@element element="faxNumber" value="${recipient.recipientFaxDelivery.documentRecipientFaxPhoneNumber!}"/>
                <@element element="faxNumberExtension" value="${recipient.recipientFaxDelivery.documentRecipientFaxPhoneNumberExtension!}"/>
                <@element element="subjectText" value="${recipient.recipientFaxDelivery.documentRecipientFaxMessage!}"/>
                <@element element="messageText" value="${recipient.recipientFaxDelivery.documentRecipientFaxSubject!}"/>
              </faxInfo> 
            </#if>
            <#if recipient.recipientEmailDelivery??>
              <emailInfo>
                <@element element="emailAddress" value="${recipient.recipientEmailDelivery.documentRecipientEmailAddress!}"/>
                <@element element="subjectText" value="${recipient.recipientEmailDelivery.documentRecipientEmailSubject!}"/>
                <@element element="messageText" value="${recipient.recipientEmailDelivery.documentRecipientEmailMessage!}"/>
              </emailInfo>
            </#if>
</#macro>  

<#macro element element value>
    <${element}><#if emptyValues?? == false><#escape x as x?xml>${value!}</#escape></#if></${element}>
</#macro>
<#macro dateElement element value> 
    <${element}><#if (value != 'null') && (emptyValues?? == false)><#escape x as x?xml>${value?datetime?string("yyyy-MM-dd HH:mm:ss")}</#escape></#if></${element}> 
</#macro>
