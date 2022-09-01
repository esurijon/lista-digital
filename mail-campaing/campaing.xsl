<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:util="xalan://com.surix.ld.util.XslExtensions"
  extension-element-prefixes="util"
   >
   
<xsl:output method="html"></xsl:output>

<xsl:template name="mail-headers"><![CDATA[Content-Type: text/html;

]]></xsl:template>

<xsl:variable name="h1">
  color:#33ADFF;
  font-family:Verdana,Arial,Helvetica,sans-serif;
  font-size:18px;
  font-weight: normal;
</xsl:variable>

<xsl:variable name="h2">
  color:#003A61; 
  font-family:'Trebuchet MS',arial,Calibri,sans-serif; 
  font-size:14px; 
  font-weight:bold;    
</xsl:variable>

<xsl:variable name="copete">
  margin-bottom: 5px; 
  margin-left: 20px; 
  padding: 15px;
  background-color: #EEEEEE;
  border: 1px solid #CCCCCC;
  color: #333333; 
  font-family: 'verdana', arial, calibri, sans-serif; 
  font-size: 12px; 
  font-weight: normal; 
  font-style: italic;
  line-height: 16px;    
</xsl:variable>

<xsl:variable name="p">
  margin-bottom: 5px; 
  margin-left: 20px; 
  color: #333333; 
  font-family: 'verdana', arial, calibri, sans-serif; 
  font-size: 12px; 
  font-weight: normal; 
  line-height: 16px;    
</xsl:variable>

<xsl:variable name="li">
  color:#333333;
  font-family:verdana,arial,calibri,sans-serif;
  font-size:12px;
  font-weight:normal;
  list-style-type:none;
</xsl:variable>

<xsl:variable name="button">
  background-color:#003A61;
  color:#ffffff;
  font-family:'Trebuchet MS',arial,Calibri,sans-serif;
  font-size:14px;
  font-weight:bold;
  padding:7px;
  text-align:center;
  text-decoration:none;
</xsl:variable>

<xsl:template name="header">
  <tr>
    <td align="center" style="border:0; padding: 0; margin: 0">
      <img src="http://www.listadigital.com.ar/img/logo-mail.png"></img>
    </td>
  </tr>
</xsl:template>

<xsl:template name="body">
  <tr>
    <td style="min-height: 500px; padding-left: 20px;">
     <xsl:apply-templates/>
    </td>
  </tr>
</xsl:template>

<xsl:template name="footer">
  <tr style="height:40px;">
    <td><![CDATA[ ]]></td>
  </tr>
  <tr style="background-color:#333333; bottom:0; height:30px; margin-top:20px;">
    <td style="color:#33ADFF; font-family:Verdana,Arial,Helvetica,sans-serif; font-size:18px; font-weight:normal;">
      <a style="margin: 8px; color:#33ADFF; display:block; font-family:Verdana,Arial,Helvetica,sans-serif; font-size:24px; line-height:25px;text-align:center; text-decoration:none;" href="http://www.listadigital.com.ar">
        www.listadigital.com.ar
      </a>
    </td>
  </tr>
</xsl:template>

<xsl:template match="/">
  <xsl:call-template name="email" />
</xsl:template>

<xsl:template name="email">
  <xsl:call-template name="mail-headers" />
  <table width="100%">
    <xsl:call-template name="header" />
    <xsl:call-template name="body" />
    <xsl:call-template name="footer" />
  </table>
</xsl:template>

<xsl:template match="data">
  <xsl:variable name="OPEN_EMAIL" select="util:encode-base64(concat('Email de campana abierto por: ', id))"/>
  <xsl:variable name="CLICK_HOME" select="util:encode-base64(concat('TRACK=', util:encode-base64(concat('CLICK en HOME link de: ', id))))"/>
  <xsl:variable name="CLICK_REGISTER" select="util:encode-base64(concat('REGISTER=true%26TRACK=', util:encode-base64(concat('CLICK en REGISTER link de: ', id))))"/>
  <xsl:variable name="CLICK_OFFERS" select="util:encode-base64(concat('TRACK=', util:encode-base64(concat('CLICK en OFFERS link de: ', id))))"/>

  <h1 style="{$h1}">
  	Estimado Organizador de eventos:
  </h1>
  <p style="{$copete}">
	Quienes organizamos eventos sabemos que la lista de invitados es una de las cosas
	que más modificaciones y cambios inesperados sufre y tenemos que correr reordenando las
	mesas, la gente, imprimiendo nuevos listados. Por eso queremos ofrecerte un servicio 
	que te ayudará en la organización de tus eventos.
  </p> 	
  <h2 style="{$h2}">
  	Un nuevo sitio en internet.
  </h2>
  <p style="{$p}">
  	Lista Digital te ofrece un servicio esencial para organizadores de eventos y salones de fiesta, como vos.
  </p>
  <p style="{$p}">
	Con Lista Digital tus clientes podrán armar las listas de invitados de sus eventos de una manera fácil y efectiva.<br/>
	Podrán agregar a cada uno de los invitados indicando mesa, menú, tipo de cubierto y alguna observación si fuese necesario.
	El armado de las mesas es muy sencillo, sólo tienen que hacer click sobre el invitado y moverlo de una mesa hacia la otra; de igual manera podrán acomodar las mesas sobre el plano de salón.   
  </p>
  <p style="{$p}">
	Con Lista Digital no es necesario recibir una copia de los listados con anticipación, tus clientes podrán realizar cambios en la lista hasta último momento y cuando
	los necesites sólo ingresás al evento y los imprimís.
  </p>
  <p style="{$p}">
	Por cada evento tendrás los siguientes listados:
  </p>

  <ul>
    <li style="{$li}">
      <table>
        <tr>
          <td><img src="http://www.listadigital.com.ar/img/check.gif" width="20"></img></td>
          <td>Listado de invitados ordenado por apellido y nombre.</td>
        </tr>
      </table>
    </li>
    <li style="{$li}">
      <table>
        <tr>
          <td><img src="http://www.listadigital.com.ar/img/check.gif" width="20"></img></td>
          <td>Listado por cada mesa (detallando: comensales, menúes, sillas para bebe, étc.)</td>
        </tr>
      </table>
    </li>
    <li style="{$li}">
      <table>
        <tr>
          <td><img src="http://www.listadigital.com.ar/img/check.gif" width="20"></img></td>
          <td>Plano con la ubicación de las mesas.</td>
        </tr>
      </table>
    </li>
    <li style="{$li}">
      <table>
        <tr>
          <td><img src="http://www.listadigital.com.ar/img/check.gif" width="20"></img></td>
          <td>Resumen general (cantidad de: invitados, mesas, menúes epeciales y observaciones pertinentes de cada invitado).</td>
        </tr>
       </table>
    </li>
  </ul>
  <h2 style="{$h2}">
    Registrate ahora!
  </h2>
 	<p style="{$p}">
	Te ofrecemos una prueba gratuita de un mes para que pruebes nuestro servicio.<br/>
	Es muy fácil, con sólo crear una cuenta indicando tu e-mail e información de contacto, ya podés comenzar a organizar tu próximo evento.
  </p>
  <p style="text-align: center; margin: 50px;">
	  <a style="{$button}" href="http://www.listadigital.com.ar/home.html?{$CLICK_REGISTER}">
	    REGISTRARME AHORA!
	  </a>
  </p>
  <p style="{$p}">
	Si al finalizar el período de prueba decidís no continuar utilizando el servicio, no tenés que hacer nada; tu cuenta se desactivará automaticámente, sin generar cargos a tu cuenta.  
  </p>
  <h2 style="{$h2}">
    Demo:
  </h2>
 	<p style="{$p}">
  	  Porque un ejemplo vale mas que mil palabras, accedé a nuestra demo y observá la lista de un evento en funcionamiento.<br/>
	  La demo contiene la lista de invitados de un evento ficticio con todas las características con las que contaras para cualquiera de tus eventos. Animate! Accedé ahora y comenzá a familiarizarte con nuestro servicio.
	</p>
	<p style="text-align: center; margin: 50px;">
	   <a style="{$button}" href="http://www.listadigital.com.ar/demo/event.html">
	     VER DEMO
	   </a>
    </p>
  <h2 style="{$h2}">
    Ofertas y servicios:
  </h2>
 	<p style="{$p}">
  	  Podés conocer más acerca de nosotros ingresando a <a href="http://www.listadigital.com.ar/home.html?{$CLICK_HOME}">www.listadigital.com.ar</a> o consultar sobre nuestros planes y servicios.
	</p>
	<p style="text-align: center; margin: 50px;">
	   <a style="{$button}" href="http://www.listadigital.com.ar/offers.html?{$CLICK_OFFERS}">
	     VER PLANES
	   </a>
    </p>
	<p>
		<img src="http://www.listadigital.com.ar/ld/track?{$OPEN_EMAIL}" height="1" width="1"></img>
	</p>
</xsl:template>

</xsl:stylesheet>
