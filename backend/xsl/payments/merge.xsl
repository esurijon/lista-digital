<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:util="xalan://com.surix.ld.util.XslExtensions"
  extension-element-prefixes="util"
   >

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />

  <xsl:param name="planner-id" />
  <xsl:variable name="payments" select="document(concat('/',$planner-id,'/payments.xml.gz'))/payments" />
  <xsl:variable name="payment-ref-id" select="substring-after(result/operation/extra_part,'-')" />
  <xsl:variable name="payment-ref" select="$payments/payment[@id=$payment-ref-id]" />
  <xsl:variable name="next-payment" select="document(concat('/',$planner-id,'/payments.xml.gz'))/payments/next-payment" />

  <xsl:template match="/">
    <payments>
      <xsl:for-each select="$payments/payment[not(@id=$payment-ref-id)]">
        <xsl:copy-of select="." />
      </xsl:for-each>
      <xsl:choose>
      	<xsl:when test="$payment-ref">
	      <xsl:call-template name="update-payment" />
      	</xsl:when>
      	<xsl:otherwise>
	      <xsl:call-template name="add-payment" />
      	</xsl:otherwise>
      </xsl:choose>
    </payments>
  </xsl:template>

  <xsl:template name="update-payment">
  	<payment>
        <xsl:copy-of select="$payment-ref/@id | $payment-ref/service | $payment-ref/period" />
  		<status>
	        <date><xsl:value-of select="util:current-date()"/></date>
	        <state><xsl:value-of select="/result/operation/status"/></state>
  		</status>
  		<mp-operation>
	        <xsl:copy-of select="/result/operation/*" />
  		</mp-operation>
  	</payment>
  	<next-payment>
  		<available><xsl:value-of select="count($payments/payment[not(@id=$payment-ref-id) and (status/state='P' or status/state='C' or status/state='R')]) = 0 and /result/operation/status = 'A'"/></available>
        <xsl:copy-of select="$next-payment/service | $next-payment/period | $next-payment/mp" />
  	</next-payment>
  </xsl:template>

  <xsl:template name="add-payment">
	<xsl:variable name="payment-operation" select="/result/operation" />
  	<payment>
  		<xsl:attribute name="id">
  			<xsl:value-of select="$payment-ref-id"/>
  		</xsl:attribute>
        <xsl:copy-of select="$next-payment/service | $next-payment/period" />
  		<status>
	        <date><xsl:value-of select="util:current-date()"/></date>
	        <state><xsl:value-of select="$payment-operation/status"/></state>
  		</status>
  		<mp-operation>
	        <xsl:copy-of select="$payment-operation/*" />
  		</mp-operation>
  	</payment>
  	<next-payment>
  		<xsl:variable name="from" select="util:roll-date($next-payment/period/from, '1M')"/>
		<xsl:variable name="to" select="util:roll-date($next-payment/period/to, '1M')"/>
  		<available><xsl:value-of select=" $next-payment/available = 'true' and $payment-operation/status = 'A' "/></available>
  		<service>BASEPACK</service>
  		<period>
  			<from><xsl:value-of select="$from"/></from>
  			<to><xsl:value-of select="$to"/></to>
  		</period>
  		<mp>
  			<checkout-url><xsl:value-of select="util:get-property('mp.buy.url')"/></checkout-url>
			<account-id><xsl:value-of select="util:get-property('mp.accountId')"/></account-id>
			<token><xsl:call-template name="enc-token"/></token>

			<item-id>BASEPACK</item-id>
			<name>BASE PACK(Periodo #<xsl:value-of select="$payment-ref-id + 1"/>)</name>

			<currency>ARG</currency>
			<price>190.0</price>

			<process-url>http://www.listadigital.com.ar/ld-static/planner/main.html?PROCESS</process-url>
			<succesfull-url>http://www.listadigital.com.ar/ld-static/planner/main.html?SUCCESFULL</succesfull-url>
			<cancel-url>http://www.listadigital.com.ar/ld-static/planner/main.html?CANCEL</cancel-url>

			<extra-part><xsl:value-of select="$planner-id"/>-<xsl:value-of select="$payment-ref-id + 1"/></extra-part>  
  		</mp>
  	</next-payment>
  </xsl:template>

  <xsl:template name="format-date">
  	<xsl:param name="date-str"/>
  	<xsl:variable name="year" select="substring($date-str,1,4)"/>
  	<xsl:variable name="month" select="substring($date-str,6,2)"/>
  	<xsl:variable name="day" select="substring($date-str,9,2)"/>
  	<xsl:value-of select="concat($day,'/',$month,'/',$year)"/>
  </xsl:template>

  <xsl:template name="enc-token">
	<xsl:variable name="message" select="concat(util:get-property('mp.accountId'), 'BASEPACK', '190.0', 'ARG', util:get-property('mp.token'))"/>
	<xsl:value-of select="util:md5($message)"/>
  </xsl:template>

 </xsl:stylesheet>
