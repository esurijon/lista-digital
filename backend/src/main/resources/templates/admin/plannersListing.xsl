<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
>

<xsl:template match="/">
  <planners>
    <xsl:apply-templates select="planners/planner[not(@id=0)]">
      <xsl:sort order="ascending" select="name"/>
    </xsl:apply-templates>
  </planners>
</xsl:template>

<xsl:template match="planner">
  <planner id="{@id}">
    <xsl:value-of select="name"/>
  </planner>
</xsl:template>

</xsl:stylesheet>