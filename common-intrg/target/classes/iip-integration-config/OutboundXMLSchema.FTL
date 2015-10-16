<?xml version="1.0" encoding="ISO-8859-1"?>
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema">
	<xsd:element name="outboundDocuments" type="outboundDocumentsType" />

	<xsd:complexType name="outboundDocumentsType">
		<xsd:sequence>
			<xsd:element name="outboundDocument" type="outboundDocumentType" minOccurs="0" maxOccurs="unbounded"/>
		</xsd:sequence>
	</xsd:complexType>

	<xsd:element name="outboundDocument" type="outboundDocumentType" />
		<xsd:complexType name="outboundDocumentType">
			<xsd:all>
			      <xsd:element name="document" type="documentType"  minOccurs="0" maxOccurs="1"/>
			      <xsd:element name="singleDataElements" type="singleDataElementsType"  minOccurs="0" maxOccurs="1"/>
			      <xsd:element name="tableDataElements" type="tableDataElementsType"  minOccurs="0" maxOccurs="1"/>
			      <xsd:element name="recipients" type="recipientsType"  minOccurs="0" maxOccurs="1"/>
			      <xsd:element name="recipient" type="recipientType" minOccurs="0" maxOccurs="1" />
			      <xsd:element name="status" type="statusType"  minOccurs="0" maxOccurs="1"/>
			      <xsd:element name="turnaroundDocuments" type="turnarooundDocumentsType" minOccurs="0" maxOccurs="1"/>
			</xsd:all>
		</xsd:complexType>
		
		<xsd:complexType name="turnarooundDocumentsType"  >
			<xsd:sequence>
				<xsd:element name="turnaroundDocument" type="turnaroundDocumentType" minOccurs="0" maxOccurs="unbounded"/>
			</xsd:sequence>
		</xsd:complexType>

		<xsd:complexType name="turnaroundDocumentType">
			<xsd:all>
				<xsd:element name="returnDocumentReferenceNumber" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="returnDocumentName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="returnDocumentExpectedReturnDays" type="xsd:int" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="returnDocumentExpectedReturnDate" type="dateType" minOccurs="0" maxOccurs="1"/>
			</xsd:all>
		</xsd:complexType>

		
		<xsd:complexType name="documentType">
			<xsd:all>
		      		<xsd:element name="agreementId" type="xsd:int" minOccurs="1" maxOccurs="1"/>
				<xsd:element name="agreementTypeCode" type="xsd:string" minOccurs="1" maxOccurs="1"/>
				<xsd:element name="agreementSubId" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="agreementSubTypeCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="categoryCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="categoryCodeName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="categoryTypeCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="categoryTypeCodeName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="createdDateTime" type="dateType" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="documentName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="documentNumber" type="xsd:int" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="documentOutboundTurnaroundDate" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="documentReferenceNumber" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="documentSourceCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="formatCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="generatedByCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="recordId" type="xsd:int" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="recordSourceCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="recordSourceCodeName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="renderedFilePath" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="sourceCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="sourceCodeName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="turnaroundDocumentRequiredInd" type="xsd:boolean" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="updatedDateTime" type="dateType" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="userIdCreated" type="xsd:int" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="userNameCreated" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="userIdUpdated" type="xsd:int" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="userNameUpdated" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="deliveryTypeCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="deliveryDeviceName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="productionGroupCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="paperStockCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="contextPackageCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="productionOrderCount" type="xsd:int" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="printDuplexInd" type="xsd:boolean" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="multipleRenderedInd" type="xsd:boolean" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="prePrintedEnclosureInd" type="xsd:boolean" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="documentTemplateName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="lineOfBusinessCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="lineOfBusinessName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="lineOfBusinessCategoryCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="lineOfBusinessCategoryName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="marketTypeCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="marketTypeName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="documentImages" type="documentImagesType" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="documentCompany" type="documentCompanyType" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="jurisdictions" type="jurisdictionsType"  minOccurs="0" maxOccurs="1"/>
			</xsd:all>
		</xsd:complexType>
		<xsd:complexType name="jurisdictionsType"  >
			<xsd:sequence>
				<xsd:element name="jurisdiction" type="jurisdictionType" minOccurs="0" maxOccurs="unbounded"/>
			</xsd:sequence>
		</xsd:complexType>

		<xsd:complexType name="jurisdictionType">
			<xsd:all>
				<xsd:element name="jurisdictionName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="jurisdictionId" type="xsd:int" minOccurs="0" maxOccurs="1"/>
		    	</xsd:all>
		</xsd:complexType>
		 
		<xsd:complexType name="documentImagesType">
			<xsd:all>
				<xsd:element name="watermarkFileName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="advertisementFileName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
		    	</xsd:all>
		</xsd:complexType>
		<xsd:complexType name="documentCompanyType">
			<xsd:all>
				<xsd:element name="corporationId" type="xsd:int" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="corporationName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="companyId" type="xsd:int" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="companyName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="companyDBAName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="companyMailingAddress" type="companyMailingAddressType" />
				<xsd:element name="companyPhoneNumbers" type="companyPhoneNumbersType"  minOccurs="0" maxOccurs="1"/>
				<xsd:element name="companyEmailAddress" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="companyWebSite" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="companySmallLogoFileName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="companyLargeLogoFileName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="companyExecutives" type="companyExecutivesType"  minOccurs="0" maxOccurs="1" />
				<xsd:element name="naicsCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="naicsGroupCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="naicsCarrierCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="isoGroupNumber" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="isoCarrierNumber" type="xsd:string" minOccurs="0" maxOccurs="1"/>
			</xsd:all>
		</xsd:complexType>
		<xsd:complexType name="companyMailingAddressType">
			<xsd:all>
				<xsd:element name="street1" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="street2" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="city" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="state" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="stateName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="postalCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="countryCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="countryName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
			</xsd:all>
		</xsd:complexType>

		<xsd:complexType name="companyPhoneNumbersType" >
			<xsd:sequence>
				<xsd:element name="phone" type="phoneType" minOccurs="0" maxOccurs="unbounded"/>
			</xsd:sequence>
		</xsd:complexType>

		<xsd:complexType name="phoneType">
			<xsd:all>
				<xsd:element name="phoneNumber" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="extension" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="type" type="xsd:string" minOccurs="0" maxOccurs="1"/>
			</xsd:all>
		</xsd:complexType>

		<xsd:complexType name="companyExecutivesType">
			<xsd:sequence>
				<xsd:element name="executive" type="executiveType" minOccurs="0" maxOccurs="unbounded" />
			</xsd:sequence>
		</xsd:complexType>

		<xsd:complexType name="executiveType">
			<xsd:all>
				<xsd:element name="title" type="xsd:string" />
				<xsd:element name="executiveName" type="xsd:string" />
				<xsd:element name="executiveSignatureFileName" type="xsd:string" />
			</xsd:all>
		</xsd:complexType>

		<xsd:complexType name="statusType">
			<xsd:all>
				<xsd:element name="createdDateTime" type="dateType" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="stateCode" type="xsd:string"  minOccurs="0" maxOccurs="1"/>
				<xsd:element name="stateName" type="xsd:string"  minOccurs="0" maxOccurs="1"/>
				<xsd:element name="statusCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="statusName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="effectiveDateTime" type="dateType" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="endDateTime" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="recordId" type="xsd:int" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="updatedDateTime" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="userIdCreated" type="xsd:int" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="userIdUpdated" type="xsd:string" minOccurs="0" maxOccurs="1"/>
			</xsd:all>
		</xsd:complexType>
		
		<xsd:complexType name="recipientsType">
			<xsd:all>
				<xsd:element name="sendToRecipient" type="recipientType" />
				<xsd:element name="ccRecipients" type="ccRecipientType" minOccurs="0" maxOccurs="1" />
			</xsd:all>
		</xsd:complexType>

		<xsd:complexType name="ccRecipientType">
			<xsd:sequence>
				<xsd:element name="ccRecipient" type="recipientType" minOccurs="0" maxOccurs="unbounded" />
			</xsd:sequence>
		</xsd:complexType>


		<xsd:complexType name="recipientType">
			<xsd:all>
				<xsd:element name="name" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="recipientRecordId" type="xsd:int" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="partyRecordId" type="xsd:int" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="deliveryType" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="participationContextCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="participationContextName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="participationType" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="participationTypeName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="specialRecipientInd" type="xsd:boolean" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="partyRoleType" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="partyRoleTypeName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="partyTypeCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="partyTypeName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="phone" type="recipientPhoneType" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="mailingAddress" type="mailingAddressType" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="recipientType" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="faxInfo" type="faxType" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="emailInfo" type="emailType" minOccurs="0" maxOccurs="1"/>
			</xsd:all>
		</xsd:complexType>

		<xsd:complexType name="mailingAddressType">
			<xsd:all>
				<xsd:element name="inCareOfName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="inCareOfRecordId" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="attentionToName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="street1" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="street2" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="city" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="state" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="stateName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="postalCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="countryCode" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="countryName" type="xsd:string" minOccurs="0" maxOccurs="1"/>
			</xsd:all>
		</xsd:complexType>

		<xsd:complexType name="recipientPhoneType">
			<xsd:all>
				<xsd:element name="phoneNumber" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="extension" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="type" type="xsd:string" minOccurs="0" maxOccurs="1"/>
			</xsd:all>
		</xsd:complexType>

		<xsd:complexType name="faxType">
			<xsd:all>
				<xsd:element name="faxNumber" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="faxNumberExtension" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="subjectText" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="messageText" type="xsd:string" minOccurs="0" maxOccurs="1"/>
			</xsd:all>
		</xsd:complexType>

		<xsd:complexType name="emailType">
			<xsd:all>
				<xsd:element name="emailAddress" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="subjectText" type="xsd:string" minOccurs="0" maxOccurs="1"/>
				<xsd:element name="messageText" type="xsd:string" minOccurs="0" maxOccurs="1"/>
			</xsd:all>
		</xsd:complexType>
  		
  		<xsd:complexType name="singleDataElementsType">
    			<xsd:all>
    				<#list singleDataElements?keys as agreementType>	
      				<xsd:element name="${agreementType}" type="single_${agreementType}_Type" minOccurs="0" maxOccurs="1"/>
      				</#list>
    			</xsd:all>
  		</xsd:complexType>

		<#list singleDataElements?keys as agreementType>
			<xsd:complexType name="single_${agreementType}_Type">
				<xsd:all>
					<#list (singleDataElements[agreementType])?keys as groupCode>
					<xsd:element name="${groupCode}" type="single_${agreementType}_${groupCode}_Type" minOccurs="0" maxOccurs="1"/>
					</#list>
				</xsd:all>
			</xsd:complexType>
		</#list>

		<#list singleDataElements?keys as agreementType>
			<#list (singleDataElements[agreementType])?keys as groupCode>
				<xsd:complexType name="single_${agreementType}_${groupCode}_Type">
				<xsd:all>
				<#list singleDataElements[agreementType][groupCode] as entry>
					<xsd:element name="${entry.docDataXMLId}" type="<#if !(entry.dataElmDataTypeCode??)>xsd:string<#elseif (entry.dataElmDataTypeCode = "varchar" 
					|| entry.dataElmDataTypeCode = "char" || entry.dataElmDataTypeCode = "charset" 
					|| entry.dataElmDataTypeCode = "date" || entry.dataElmDataTypeCode = "time"
					|| entry.dataElmDataTypeCode = "money")>xsd:string<#elseif (entry.dataElmDataTypeCode = "nbr")>xsd:long<#elseif (entry.dataElmDataTypeCode = "datetm")>dateType<#elseif (entry.dataElmDataTypeCode = "dec")>xsd:decimal<#else>xsd:${entry.dataElmDataTypeCode}</#if>" minOccurs="0" maxOccurs="1" />
				</#list>
				</xsd:all>
				</xsd:complexType>
			</#list>
		</#list>
			
		<xsd:complexType name="tableDataElementsType">
			<xsd:all>
    				<#list tableDataElements?keys as agreementType>	
      				<xsd:element name="${agreementType}" type="table_agreement_${agreementType}_Type" minOccurs="0" maxOccurs="1"/>
      				</#list>
			</xsd:all>
		</xsd:complexType>

		<#list tableDataElements?keys as agreementType>
			<xsd:complexType name="table_agreement_${agreementType}_Type">
				<xsd:all>
					<#list (tableDataElements[agreementType])?keys as groupCode>
					<xsd:element name="${groupCode}" type="table_agree_grp_${agreementType}_${groupCode}_Type" minOccurs="0" maxOccurs="1"/>
					</#list>
				</xsd:all>
			</xsd:complexType>
		</#list>

		<#list tableDataElements?keys as agreementType>
			<#list (tableDataElements[agreementType])?keys as groupCode>
				<xsd:complexType name="table_agree_grp_${agreementType}_${groupCode}_Type">
					<xsd:all>
						<#list tableDataElements[agreementType][groupCode] as entry>
						<xsd:element name="${entry.docDataXMLId}" type="${entry.docDataXMLId}_Type" minOccurs="0" maxOccurs="1" />
						</#list>
					</xsd:all>
				</xsd:complexType>
			</#list>
		</#list>
		
		<#list tableDataElements?keys as agreementType>
			<#list (tableDataElements[agreementType])?keys as groupCode>
				
				<#list tableDataElements[agreementType][groupCode] as entry>
					<xsd:complexType name="${entry.docDataXMLId}_Type">
						<xsd:all>
							<xsd:element name="table" type="tableElement_${agreementType}_${groupCode}_${entry.docDataXMLId}_Type" minOccurs="0" maxOccurs="1" />
						</xsd:all>
					</xsd:complexType>
				</#list>
			</#list>
		</#list>
		
		
		
		
		
		<#list tableDataElements?keys as agreementType>
			<#list (tableDataElements[agreementType])?keys as groupCode>
				<#list tableDataElements[agreementType][groupCode] as entry>
				<xsd:complexType name="tableElement_${agreementType}_${groupCode}_${entry.docDataXMLId}_Type">
				<xsd:all>
					<#if groupedColumns[entry.docDataElementCode]??>
						<#list groupedColumns[entry.docDataElementCode] as groupedColumn>
							<xsd:element name="${groupedColumn.xmlId}_group" type="table_${agreementType}_${groupCode}_${groupedColumn.xmlId}_Type" minOccurs="0" maxOccurs="1"/>
						</#list>
					<#else>
						<xsd:element name="row" type="rowElement_${agreementType}_${groupCode}_${entry.docDataXMLId}_Type" minOccurs="0" maxOccurs="1" />
					</#if>
				</xsd:all>
				</xsd:complexType>
				</#list>
			</#list>
		</#list>
		
		
		<#list tableDataElements?keys as agreementType>
			<#list (tableDataElements[agreementType])?keys as groupCode>
				<#list tableDataElements[agreementType][groupCode] as entry>
				<#if groupedColumns[entry.docDataElementCode]??>
					<#list groupedColumns[entry.docDataElementCode] as groupedColumn>
					<xsd:complexType name="table_${agreementType}_${groupCode}_${groupedColumn.xmlId}_Type">
						<xsd:all>
							<xsd:element name="${groupedColumn.xmlId}" type="xsd:string" minOccurs="0" maxOccurs="1"/>
							<xsd:element name="row" type="rowElement_${groupCode}_${groupedColumn.xmlId}_Type" minOccurs="0" maxOccurs="1" />
						</xsd:all>
					</xsd:complexType>
					</#list>
				</#if>
				</#list>
			</#list>
		</#list>

		<#list tableDataElements?keys as agreementType>
			<#list (tableDataElements[agreementType])?keys as groupCode>
				<#list tableDataElements[agreementType][groupCode] as entry>
					<#if groupedColumns[entry.docDataElementCode]??>
						<#list groupedColumns[entry.docDataElementCode] as groupedColumn>
							<xsd:complexType name="rowElement_${groupCode}_${groupedColumn.xmlId}_Type">
							    <#if tableColumnDataElements ?? && tableColumnDataElements[entry.docDataElementCode]??>
								<xsd:all>
								    <#list tableColumnDataElements[entry.docDataElementCode] as column>
									<xsd:element name="${column.xmlId}" type="xsd:string" minOccurs="0" maxOccurs="1"/>
								    </#list>
								</xsd:all>
							    </#if>
							</xsd:complexType>
						</#list>
					<#elseif !groupedColumns[entry.docDataElementCode]??>
						<xsd:complexType name="rowElement_${agreementType}_${groupCode}_${entry.docDataXMLId}_Type">
						    <#if tableColumnDataElements ?? && tableColumnDataElements[entry.docDataElementCode]??>
							<xsd:all>
							    <#list tableColumnDataElements[entry.docDataElementCode] as column>
								<#if column.groupByIndicator == false || column.groupByPosition?? == false>
									<xsd:element name="${column.xmlId}" type="xsd:string" minOccurs="0" maxOccurs="1"/>
								</#if>
							    </#list>
							</xsd:all>
						    </#if>
						</xsd:complexType>
					</#if>
				</#list>
			</#list>
		</#list>
		
		
		<xsd:simpleType name="dateType">
			<xsd:restriction base="xsd:string">
				<xsd:pattern
				value="[0-9]{4}-[01][0-9]-[0-3][0-9] [0-2][0-9]:[0-5][0-9]:[0-6][0-9]" />
			</xsd:restriction>
		</xsd:simpleType>
</xsd:schema>