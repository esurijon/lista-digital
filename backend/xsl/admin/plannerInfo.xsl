<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="planner-id" />

  <xsl:template match="/">
    <xsl:apply-templates select="/planners/planner[@id=$planner-id]" />
  </xsl:template>

  <xsl:template match="planner">
    <planner id="{@id}">
      <xsl:apply-templates />
    </planner>
  </xsl:template>

  <xsl:template match="password">
  </xsl:template>

  <xsl:template match="resetToken">
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>