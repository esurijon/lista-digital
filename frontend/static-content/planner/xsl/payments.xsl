<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:data="http://www.listadigital.com.ar/data"
   exclude-result-prefixes="data">

<data:months>
     <month>Ene</month>
     <month>Feb</month>
     <month>Mar</month>
     <month>Abr</month>
     <month>May</month>
     <month>Jun</month>
     <month>Jul</month>
     <month>Ago</month>
     <month>Sep</month>
     <month>Oct</month>
     <month>Nov</month>
     <month>Dic</month>
</data:months>

<data:status>
     <state id="A"><label>Acreditado</label><style>color: #128C45; font-weight: bold;</style></state>
     <state id="B"><label>Bonificado</label><style>color: #128C45; font-weight: bold;</style></state>
     <state id="C"><label>Cancelado</label><style>color: #FF0000; font-weight: bold;</style></state>
     <state id="R"><label>Rechazado</label><style>color: #FF0000; font-weight: bold;</style></state>
     <state id="P"><label>En progreso</label><style>color: #8C2525; font-weight: bold;</style></state>
</data:status>

<data:services>
     <service id="BASEPACK">BASE PACK</service>
</data:services>

	<xsl:template match="/">
		<xsl:apply-templates select="payments" />
	</xsl:template>

	<xsl:template match="payments">

		<xsl:apply-templates select="next-payment"/>

		<table id="plain-view">
			<thead>
				<tr>
				  <td class="head1" colspan="2">PAGO</td>
				  <td class="head2" rowspan="2">SERVICIO</td>
				  <td class="head3" colspan="2">PERÍODO DE VALIDEZ</td>
				</tr>
				<tr>
				  <td class="head1">FECHA</td>
				  <td class="head1">ESTADO</td>
				  <td class="head1">DESDE</td>
				  <td class="head1">HASTA</td>
				</tr>
			</thead>
			<tbody class="plain">
				<xsl:apply-templates select="payment">
					<xsl:sort select="@id" data-type="number" order="descending"/>
				</xsl:apply-templates>
			</tbody>
		</table>
	</xsl:template>

  <xsl:template match="payment">
    <tr id="{@id}">
      <xsl:attribute name="class">
        item <xsl:call-template name="zebra"></xsl:call-template>
      </xsl:attribute>
      <td name="" class="col4">
		<xsl:call-template name="format-date">
			<xsl:with-param name="date-str" select="status/date"/>
		</xsl:call-template>
      </td>
      <xsl:variable name="st" select="status/state"/>
      <td name="" class="col4" style="{document('')/*/data:status/state[@id=$st]/style}">
         <xsl:value-of select="document('')/*/data:status/state[@id=$st]/label"/>
      </td>
      <td name="service" class="col3">
        <xsl:variable name="srv" select="service" />
        <xsl:value-of select="document('')/*/data:services/service[@id=$srv]"/>
      </td>
      <td name="period.from" class="col4">
		<xsl:call-template name="format-date">
			<xsl:with-param name="date-str" select="period/from"/>
		</xsl:call-template>
      </td>
      <td name="period.to" class="col5">
		<xsl:call-template name="format-date">
			<xsl:with-param name="date-str" select="period/to"/>
		</xsl:call-template>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="next-payment">
	<form target="MercadoPago" action="{mp/checkout-url}" method="post" class="next-payment">
		<h1>
			PRÓXIMO VENCIMIENTO: 
			<xsl:call-template name="format-date">
				<xsl:with-param name="date-str" select="period/from"/>
			</xsl:call-template>
		</h1>
		<div style="margin-left: 15px; margin-top: 5px; margin-bottom: 5px;">
		Servicio:<b><xsl:apply-templates select="service"/></b><br/>
		Valido desde <b> 
		<xsl:call-template name="format-date">
			<xsl:with-param name="date-str" select="period/from"/>
		</xsl:call-template></b>, hasta 
		<b><xsl:call-template name="format-date">
			<xsl:with-param name="date-str" select="period/to"/>
		</xsl:call-template></b>
  	<xsl:choose>
  		<xsl:when test="available = 'true'">
			<input type="hidden" name="acc_id" value="{mp/account-id}"/>
			<input type="hidden" name="enc" value="{mp/token}"/>
	
			<input type="hidden" name="item_id" value="{mp/item-id}"/>
			<input type="hidden" name="name" value="{mp/name}"/>
	
			<input type="hidden" name="price" value="{mp/price}"/>
			<input type="hidden" name="currency" value="{mp/currency}"/>
	
			<input type="hidden" name="ship_cost_mode" value=""/>
			<input type="hidden" name="op_retira" value=""/>
	
			<input type="hidden" name="url_process"    value="{mp/process-url}"/>
			<input type="hidden" name="url_succesfull" value="{mp/succesfull-url}"/>
			<input type="hidden" name="url_cancel"     value="{mp/cancel-url}"/>
			<input type="hidden" name="extra_part"     value="{mp/extra-part}"/>
			<br/>	
			<input type="submit" name="checkout" value="Pagar con Mercado Pago" class="nxt-payment-button" />

  		</xsl:when>
  		<xsl:otherwise>
			<xsl:if test="//payment[status/state = 'P']">
				<h1 style="color: #8C2525; margin: 10px; font-size: 16px;">Aun tiene pagos pendientes. Podrá abonar el próximo periodo cuando todos sus pagos estén acreditados.</h1>
			</xsl:if>
			<xsl:if test="//payment[status/state = 'C' or status/state = 'R']">
				<h1 style="color: #8C2525; margin: 10px; font-size: 16px;">Pagos anteriores han sido rechazados o cancelados. Regularice su situación para poder abonar el próximo periodo.</h1>
			</xsl:if>
  		</xsl:otherwise>
  	</xsl:choose>
  	</div>
	</form>
  </xsl:template>

  <xsl:template name="zebra">
    <xsl:choose>
      <xsl:when test="position() mod 2 = 0">even</xsl:when>
      <xsl:otherwise>odd</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  
  <xsl:template name="format-date">
  	<xsl:param name="date-str"/>
  	<xsl:variable name="year" select="substring($date-str,1,4)"/>
  	<xsl:variable name="month" select="document('')/*/data:months/month[number(substring($date-str,6,2))]"/>
  	<xsl:variable name="day" select="substring($date-str,9,2)"/>
  	<xsl:value-of select="concat($day,' ',$month,' ',$year)"/>
  </xsl:template>

</xsl:stylesheet>
