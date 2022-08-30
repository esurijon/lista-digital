<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:key name="unique-places" match="guest" use="table" />
<xsl:key name="unique-cubierto" match="guest" use="cubierto" />
<xsl:key name="unique-menu" match="guest" use="menu" />

<xsl:template match="/">
  <xsl:apply-templates select="event/guests" />
</xsl:template>

<xsl:template match="guests">

  <div id="summ">

	 <table>
	   <thead>
       <tr>
  	     <th colspan="2">Cubiertos</th> 
  	   </tr>
	   </thead>
	   <tbody>
  	   <tr>
  	     <td class="col1">Total</td><td class="col2"><xsl:value-of select="sum(//guest[not(@state='D')]/quantity)" /></td>
  	   </tr>
  	   <xsl:for-each select="guest[count(. | key('unique-cubierto', cubierto)[1]) = 1]">
  	     <xsl:variable name="cubierto" select="cubierto" />
  	     <tr>
  	       <td class="col1"><xsl:value-of select="cubierto"/></td><td class="col2"><xsl:value-of select="sum(//guest[not(@state='D') and cubierto=$cubierto]/quantity)" /></td>
  	     </tr>
  	   </xsl:for-each>
  	   <tr>
  	     <td class="col1">Confirmados</td><td class="col2"><xsl:value-of select="sum(//guest[not(@state='D') and confirmed = 'true']/quantity)" /></td>
  	   </tr>
	   </tbody>
	 </table>
  
   <table>
     <thead>
       <tr>
         <th colspan="2">Menues</th> 
       </tr>
     </thead>
     <tbody>
       <tr>
         <td class="col1">Total</td><td class="col2"><xsl:value-of select="sum(//guest[not(@state='D')]/quantity)" /></td>
       </tr>
	     <xsl:for-each select="guest[count(. | key('unique-menu', menu)[1]) = 1]">
	       <xsl:variable name="menu" select="menu" />
         <tr>
           <td class="col1"><xsl:value-of select="menu"/></td><td class="col2"><xsl:value-of select="sum(//guest[not(@state='D') and menu=$menu]/quantity)" /></td>
         </tr>
       </xsl:for-each>
     </tbody>
   </table>
   
   <table>
     <thead>
       <tr>
         <th colspan="2">Mesas</th> 
       </tr>
     </thead>
     <tbody>
       <tr>
         <td class="col1">Total Mesas</td><td class="col2"><xsl:value-of select="count(guest[count(. | key('unique-places', table)[1]) = 1])" /></td>
       </tr>
	     <tr>
	       <td class="col1">Sillas Bebé</td><td class="col2"><xsl:value-of select="count(//guest[not(@state='D') and babychair='true']/quantity)" /></td>
	     </tr>
     </tbody>
   </table>

   <div style="clear: both"></div>

   <table>
     <thead>
       <tr>
         <th colspan="2">Observaciones</th> 
       </tr>
	     <tr>
	       <th>Nombre</th><th>Comentario</th>
	     </tr>
     </thead>
     <tbody>
       <xsl:choose>
        <xsl:when test="count(guest[string-length(comments)>0])>0">
         <xsl:apply-templates select="guest[string-length(comments)>0]"/>
        </xsl:when>
        <xsl:otherwise>
          <tr>
            <td colspan="2" class="guest-col1"></td>
          </tr>
        </xsl:otherwise>
       </xsl:choose>
     </tbody>
   </table>

  </div>
</xsl:template>

<xsl:template match="guest">
  <tr>
    <td class="guest-col1"><xsl:value-of select="lastname" />, <xsl:value-of select="name" /></td>
    <td class="guest-col2"><xsl:value-of select="comments" /></td>
  </tr>
</xsl:template>

</xsl:stylesheet>
