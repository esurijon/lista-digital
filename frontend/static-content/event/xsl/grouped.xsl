<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">

    <div id="grouped-view">
     <p class="noprint">Haga click sobre un invitado y arrástrelo a la mesa deseada <a id="add-table" href="#" style="float:right">Agregar mesa+</a></p>
      <div class="table-col">
        <xsl:call-template name="no-table"/>
        <xsl:apply-templates select="event/tables/table[@id mod 3 = 1]">
            <xsl:sort select="@id" data-type="number" order="ascending"/>    
        </xsl:apply-templates>
      </div>
      <div class="table-col">
        <xsl:apply-templates select="event/tables/table[@id mod 3 = 2]">
            <xsl:sort select="@id" data-type="number" order="ascending"/>    
        </xsl:apply-templates>
      </div>
      <div class="table-col">
        <xsl:apply-templates select="event/tables/table[@id mod 3 = 0]">
            <xsl:sort select="@id" data-type="number" order="ascending"/>    
        </xsl:apply-templates>
      </div>
    </div>

</xsl:template>

<xsl:template match="table">    
  <xsl:variable name="table" select="@id"></xsl:variable>
  <div id="table-{$table}" class="table-cont">
     <table class="summary">
       <tr>
         <th>Mesa <xsl:value-of select="$table"/>: <xsl:value-of select="/event/tables/table[@id=$table]/description" /></th> 
         <th><button class="pin"/></th> 
       </tr>
       <tr>
         <th>#Cubiertos: <span class="cubiertos"><xsl:value-of select="sum(//guest[not(@state='D') and table=$table]/quantity)" /></span></th> 
         <th>#Silla bebé: <span class="babychair"><xsl:value-of select="count(//guest[not(@state='D') and table=$table and babychair='true'])" /></span></th> 
       </tr>
     </table>
     <table class="detail">
       <tr class="grp-hdr">
         <th>Nombre</th> 
         <th>Cant.</th> 
         <th>Menú</th> 
       </tr>
       <xsl:apply-templates select="//guest[table=$table]">
         <xsl:sort select="lastname" data-type="text"/>        
       </xsl:apply-templates>
       <xsl:if test="count(//guest[table=$table]) = 0">
        <tr id="empty">
          <td class="grp-col1"></td>
          <td class="grp-col2"></td>
          <td class="grp-col3"></td>
        </tr>
       </xsl:if>
     </table>
  </div>
</xsl:template>

<xsl:template name="no-table">    
  <div id="table-0" class="no-table">
     <table class="unasigned">
       <tr>
         <th>Invitados sin mesa</th> 
         <th><button class="pin"/></th> 
       </tr>
       <tr>
         <th>#Cubiertos: <span class="cubiertos"><xsl:value-of select="sum(//guest[not(@state='D') and table=0]/quantity)" /></span></th> 
         <th>#Silla bebé: <span class="babychair"><xsl:value-of select="count(//guest[not(@state='D') and table=0 and babychair='true'])" /></span></th> 
       </tr>
     </table>
     <table class="detail">
       <tr class="grp-hdr">
         <th>Nombre</th> 
         <th>Cant.</th> 
         <th>Menú</th> 
       </tr>
        <xsl:apply-templates select="//event/guests/guest[table = 0]">
            <xsl:sort select="lastname" data-type="text" order="ascending"/>    
        </xsl:apply-templates>
        <xsl:if test="count(//event/guests/guest[table = 0]) = 0">
        <tr id="empty">
          <td class="grp-col1"></td>
          <td class="grp-col2"></td>
          <td class="grp-col3"></td>
        </tr>
        </xsl:if>
     </table>
  </div>
</xsl:template>

<xsl:template match="guest">
  <tr>
    <xsl:attribute name="class">
      item <xsl:call-template name="zebra"/>
    </xsl:attribute>
    <xsl:attribute name="bc">
      <xsl:value-of select="babychair"/>
    </xsl:attribute>
    <xsl:attribute name="qt">
      <xsl:value-of select="quantity"/>
    </xsl:attribute>
  	<xsl:attribute name="id">
       <xsl:value-of select="@id" />
    </xsl:attribute>
    <td class="grp-col1"><xsl:value-of select="lastname" />, <xsl:value-of select="name" /></td>
    <td class="grp-col2"><xsl:value-of select="quantity"/></td>
    <td class="grp-col3"><xsl:value-of select="menu"/></td>
  </tr>
</xsl:template>

<xsl:template name="zebra">
  <xsl:choose>
    <xsl:when test="position() mod 2 = 0">even</xsl:when>
    <xsl:otherwise>odd</xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
