<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:util="xalan://com.surix.ld.util.XslExtensions"
  extension-element-prefixes="util"
   >

<xsl:variable name="file-content">
  <![CDATA[<?xml version="1.0" encoding="UTF-8" ?><planner><events/><halls/></planner>]]>
</xsl:variable>

<xsl:template match="/">
    <xsl:apply-templates />
</xsl:template>

<xsl:template match="planners">
  <planners>
    <xsl:variable name ="ids" select="planner[@state='U' or @state='D' or @state='N']/@id"/>
    <xsl:for-each select="document('planners.xml.gz')/planners/planner[not(@id=$ids)]">
      <xsl:copy-of select="."/>
    </xsl:for-each>

    <xsl:apply-templates select="planner[@state='U' or @state='D' or @state='N']"/>
  </planners>
</xsl:template>

<xsl:template match="planner[@state='N']">
  <xsl:variable name="id" select="util:create-id()"/>
  <xsl:variable name="token" select="util:create-token()"/>

  <planner id="{$id}" state="E">
    <!-- Create planner storage -->
    <xsl:variable name="create-storage-result" select="util:create-storage(concat($id,'/events.xml.gz'),$file-content)" />
	<xsl:variable name="payments-file-content">
<![CDATA[
<?xml version="1.0" encoding="UTF-8" ?>
<payments>
	<payment id="1">
		<service>BASEPACK</service>
		<period>
			<from>]]><xsl:value-of select="util:current-date()"/><![CDATA[</from>
			<to>]]><xsl:value-of select="util:roll-date(util:roll-date(util:current-date(),'1M'),'-1d')"/><![CDATA[</to>
		</period>
		<status>
			<date>]]><xsl:value-of select="util:current-date()"/><![CDATA[</date>
			<state>B</state>
		</status>
		<mp-operation />
	</payment>
	<next-payment>
		<available>true</available>
		<service>BASEPACK</service>
		<period>
			<from>]]><xsl:value-of select="util:roll-date(util:current-date(),'1M')"/><![CDATA[</from>
			<to>]]><xsl:value-of select="util:roll-date(util:roll-date(util:current-date(),'2M'),'-1d')"/><![CDATA[</to>
		</period>
		<mp>
			<checkout-url>]]><xsl:value-of select="util:get-property('mp.buy.url')"/><![CDATA[</checkout-url>
			<account-id>]]><xsl:value-of select="util:get-property('mp.accountId')"/><![CDATA[</account-id>
			<token>]]><xsl:call-template name="enc-token"/><![CDATA[</token>
			<item-id>BASEPACK</item-id>
			<name>BASE PACK (Periodo #2)</name>
			<currency>ARG</currency>
			<price>190.0</price>
			<process-url>http://www.listadigital.com.ar/ld-static/planner/main.html?PROCESS</process-url>
			<succesfull-url>http://www.listadigital.com.ar/ld-static/planner/main.html?SUCCESFULL</succesfull-url>
			<cancel-url>http://www.listadigital.com.ar/ld-static/planner/main.html?CANCEL</cancel-url>
			<extra-part>]]><xsl:value-of select="$id"/><![CDATA[-2</extra-part>
		</mp>
	</next-payment>
</payments>
]]>
	</xsl:variable>
    <xsl:variable name="create-payments-storage-result" select="util:create-storage(concat($id,'/payments.xml.gz'),$payments-file-content)" />
    <xsl:choose>
      <xsl:when test="string-length($create-storage-result)>0">
        <error type="CREATE_STORAGE_ERROR"><xsl:value-of select="$create-storage-result"/></error>
      </xsl:when>
      <xsl:otherwise>
	    <xsl:choose>
	      <xsl:when test="string-length($create-payments-storage-result)>0">
	        <error type="CREATE_STORAGE_ERROR"><xsl:value-of select="$create-payments-storage-result"/></error>
	      </xsl:when>
	      <xsl:otherwise>
	        <!-- Send activation email -->
	        <xsl:variable name="send-activate-planner-account-mail-result" select="util:send-activate-planner-account-mail(user, name, $token)" />
	        <xsl:if test="string-length($send-activate-planner-account-mail-result)>0">
	          <error type="SEND_ACTIVATE_ACCOUNT_MAIL_ERROR"><xsl:value-of select="$send-activate-planner-account-mail-result"/></error>
	        </xsl:if>
	      </xsl:otherwise>
	    </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
    <password><xsl:value-of select="$token"/></password>
    <resetToken><xsl:value-of select="$token"/></resetToken>
    <xsl:apply-templates select="*[name() != 'password' or name() != 'resetToken']"/>
  </planner>
</xsl:template>

<!-- Updated nodes state should be set to 'E'-->
<xsl:template match="planner[@state='U']">
  <planner id="{@id}" state="E">
    <xsl:apply-templates />
  </planner>
</xsl:template>

<xsl:template match="planner[@state='D']">
  <xsl:variable name="delete-storage-result" select="util:delete-storage(@id)" />
  <xsl:if test="string-length($delete-storage-result)>0">
    <planner id="{@id}" state="E">
      <error><xsl:value-of select="$delete-storage-result"/></error>
      <xsl:apply-templates />
    </planner>
  </xsl:if>
</xsl:template>

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template name="enc-token">
  <xsl:variable name="message" select="concat(util:get-property('mp.accountId'), 'BASEPACK', '190.0', 'ARG', util:get-property('mp.token'))"/>
  <xsl:value-of select="util:md5($message)"/>
</xsl:template>

</xsl:stylesheet>