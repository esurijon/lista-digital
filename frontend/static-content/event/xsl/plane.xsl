<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="/">
    <xsl:apply-templates select="event/tables" />
  </xsl:template>

  <xsl:param name="plane-url">/ld-static/planner-assets/hall.png</xsl:param>

  <xsl:template match="tables">
   <p class="noprint">Modifique la disposición de las mesas haciendo click sobre ellas y arrastrándolas<a id="add-table" href="#" style="float:right;">Agregar mesa+</a><a id="add-plane" href="#" style="float:right; margin-right:10px;">Subir plano+</a></p>
   <div id="map">
     <img src="{$plane-url}" style="position: absolute; top: 0px; left: 0px; z-index: -1;" width="100%" height="100%"></img>
	   <xsl:apply-templates />
   </div>
   <script></script>
  </xsl:template>
	
	<xsl:template match="tables/table">
    <xsl:variable name="style">
	     <xsl:if test="left != '' and top != ''">
         left: <xsl:value-of select="left"/>px; top: <xsl:value-of select="top" />px
       </xsl:if>
    </xsl:variable>
    <div class="{shape}" id="{@id}" title="{description}">
      <xsl:attribute name="style"><xsl:value-of select="$style" /></xsl:attribute>
      <xsl:attribute name="positioned"><xsl:value-of select="left != '' and top != ''"/></xsl:attribute>
      <xsl:value-of select="@id" />                
    </div>
	</xsl:template>

</xsl:stylesheet>
