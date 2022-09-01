<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:util="xalan://com.surix.ld.util.XslExtensions"
  extension-element-prefixes="util"
   >

<xsl:variable name="hall-id" select="util:create-id()"/>

<xsl:template match="/">
<planner>
	<events>
	    <xsl:apply-templates select="event"/>
	</events>
	<halls>
		<hall id="{$hall-id}" state="N">
			<plane>/planner-assets/hall.png</plane>
			<name>
        <xsl:value-of select="//hosts/host[1]/lastname"/>_<xsl:value-of select="//hosts/host[2]/lastname"/>_<xsl:value-of select="//date"/>
      </name>
		</hall>
	</halls>
</planner>

</xsl:template>

<xsl:template match="event">
		<event id="{util:create-id()}" state="N">
			<type><xsl:value-of select="type"/></type>
			<date><xsl:value-of select="date"/></date>
			<hall><xsl:value-of select="$hall-id"/></hall>
			<hosts>
				<xsl:apply-templates select="hosts/host"/>
			</hosts>
			<permissions>
				<xsl:variable name="expiration" select="util:roll-date(util:current-date(),'1w')"/>
				<permission type="readEnabled" granted="true" expires="{$expiration}"/>
				<permission type="writeEnabled" granted="true" expires="{$expiration}"/>
			</permissions>
		</event>
</xsl:template>

<xsl:template match="host">
	<host type="{@type}">
		<name><xsl:value-of select="name"/></name>
		<lastname><xsl:value-of select="lastname"/></lastname>
		<user><xsl:value-of select="user"/></user>
		<phone><xsl:value-of select="phone"/></phone>
		<cellphone><xsl:value-of select="cellphone"/></cellphone>
	</host>
</xsl:template>

</xsl:stylesheet>