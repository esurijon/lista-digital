<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:include href="templates.xsl"/>

<xsl:output method="html"></xsl:output>

<xsl:template match="data">
  <h1 style="{$h1}">
    Bienvenido a Lista Digital.
  </h1>

  <h2 style="{$h2}">
    Activación de cuenta:
  </h2>

  <p style="{$p}">
    Estimado <b><xsl:value-of select="planner"/></b>, recibimos tu solicitud para registrarte en Lista Digital, queremos darte la bienvenida y agradecerte por elegirnos.
  </p>
  <p style="{$p}">
    Lista Digital es un servicio que te ayuda a organizarte en tu trabajo. En nuestra página web encontraras todas las facilidades para manejar las listas de invitados de cada uno de tus eventos. Con Lista Digital tus clientes vos y tendrán acceso las listas de invitados para modificarlas o imprimirlas en cualquier momento, ya que cada vez que das de alta un nuevo evento se crea una cuenta de usuario en Lista Digital para cada anfitrión del mismo.
  </p>
  <p style="{$p}">
    Solo te resta activar tu cuenta y ya podrás comenzar a cargar tus eventos.
  </p>
  <p style="text-align: center; margin: 50px;">
    <a style="{$button}">
      <xsl:attribute name="href"><xsl:value-of select="activationUrl"/></xsl:attribute>
      ACTIVAR CUENTA
    </a>
  </p>
  <p style="{$p}">
    Recuerda que para iniciar sesión en Lista Digital deberás identificarte indicando el nombre de Salón/Organizador y tu e-mail. 
    <ul style="list-style: circle;">
      <li>Organizador: <b><xsl:value-of select="planner"/></b></li>
      <li>E-mail: <b><xsl:value-of select="user"/></b></li>
      <li>Contraseña: <b>****** (la crearas al activar la cuenta)</b></li>
    </ul>
  </p>
</xsl:template>

</xsl:stylesheet>
