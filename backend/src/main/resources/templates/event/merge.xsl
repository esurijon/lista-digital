<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:util="xalan://com.surix.ld.util.FileUtil"
	xmlns:sys="xalan://java.lang.System"
	extension-element-prefixes="util"
>

<xsl:param name="event-id" />

<xsl:template match="/">
    <xsl:apply-templates />
</xsl:template>

<xsl:template match="guests">
	<guests>
		<xsl:apply-templates select="guest[not(@state='E')]"/>

		<xsl:variable name ="ids" select="guest[not(@state='E')]/@server-id"/>
		<xsl:for-each select="document(concat('/',$event-id,'.xml.gz'))/event/guests/guest[not(@id=$ids)]">
			<xsl:copy-of select="."/>
		</xsl:for-each>
	</guests>
</xsl:template>

<!-- Updated nodes state should be set to 'E'-->
<xsl:template match="guest[@state='U']">
	<guest id="{@id}" state="E">
		<xsl:apply-templates />
	</guest>
</xsl:template>

<!-- Inserted nodes state should be set to 'E'-->
<xsl:template match="guest[@state='N']">
  <guest id="{@id}" state="E">
    <xsl:apply-templates />
  </guest>
</xsl:template>

<!-- Deleted nodes may not be saved -->
<xsl:template match="guest[@state='D']" />

<xsl:template match="tables">
  <tables>
    <xsl:apply-templates select="table[not(@state='E')]"/>

    <xsl:variable name ="ids" select="table[not(@state='E')]/@server-id"/>
    <xsl:for-each select="document(concat('/',$event-id,'.xml.gz'))/event/tables/table[not(@id=$ids)]">
      <xsl:copy-of select="."/>
    </xsl:for-each>
  </tables>
</xsl:template>

<!-- Updated nodes state should be set to 'E'-->
<xsl:template match="table[@state='U']">
  <table id="{@id}" state="E">
    <xsl:apply-templates />
  </table>
</xsl:template>

<!-- Inserted nodes state should be set to 'E'-->
<xsl:template match="table[@state='N']">
  <table id="{@id}" state="E">
    <xsl:apply-templates />
  </table>
</xsl:template>

<!-- Deleted nodes may not be saved -->
<xsl:template match="table[@state='D']" />

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
