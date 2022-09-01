package com.surix.ld.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.surix.ld.exceptions.LdException;
import com.surix.ld.model.Admin;
import com.surix.ld.model.EntityException;
import com.surix.ld.model.FileEntity;
import com.surix.ld.model.Planner;
import com.surix.ld.util.Config;
import com.surix.ld.util.FileUtil;
import com.surix.ld.util.XMLUtils;

@SuppressWarnings("serial")
public class Payments extends OnLineListsServlet {

	private static final String PAYMENT_ACCEPTED_XPATH = "//operation/status = 'A'";
	private static final String EXTRA_PART_XPATH = "//operation/extra_part";
	private SimpleDateFormat dateFormat;

	private enum PaymentMethod {
		TCO, OFF
	}

	private enum Status {
		A, P, C, R
	}

	private static class Payment {

		private String sellerOpId; // seller_op_id: C�digo de referencia �nico de la operaci�n
		private String mpOpId; // mp_op_id: C�digo de referencia de la operaci�n en MercadoPago
		private long accId; // acc_id: N�mero de cuenta.
		private Status status; // status: Estado del pago: A: acreditado; P: pendiente; C: cancelado o R: rechazado
		private String itemId; // item_id: C�digo del producto
		private String name; // name: Nombre del producto
		private double price; // price: Precio del producto
		private double shippingAmount; // shipping_amount: Costo de env�o
		private double totalAmount; // total_amount: Pago total del comprador
		private String extraPart; // extra_part: Par�metros adicionales, agregados por el vendedor en el carrito.
		private PaymentMethod method; // payment_method: Medio de pago: TCO (Tarjetas de cr�dito online); OFF (Medios offline: Pago F�cil, Rapipago, Red Link, Banelco, BaproPagos, PagoMisCuentas, transferencia bancaria, dep�sito bancario, etc.).

		private Payment() {
		}

		public Payment(HttpServletRequest request) {
			try {
				sellerOpId = request.getParameter("seller_op_id");
				mpOpId = request.getParameter("mp_op_id");
				accId = Long.parseLong(request.getParameter("acc_id"));
				status = Status.valueOf(request.getParameter("status"));
				itemId = request.getParameter("item_id");
				name = request.getParameter("name");
				price = Double.parseDouble(request.getParameter("price"));
				shippingAmount = Double.parseDouble(request.getParameter("shipping_amount"));
				totalAmount = Double.parseDouble(request.getParameter("total_amount"));
				extraPart = request.getParameter("extra_part");
				method = PaymentMethod.valueOf(request.getParameter("payment_method"));
			} catch (Exception e) {
				logger.error(e);
			}
		}

		public String getSellerOpId() {
			return sellerOpId;
		}

		public String getMpOpId() {
			return mpOpId;
		}

		public long getAccId() {
			return accId;
		}

		public Status getStatus() {
			return status;
		}

		public String getItemId() {
			return itemId;
		}

		public String getName() {
			return name;
		}

		public double getPrice() {
			return price;
		}

		public double getShippingAmount() {
			return shippingAmount;
		}

		public double getTotalAmount() {
			return totalAmount;
		}

		public String getExtraPart() {
			return extraPart;
		}

		public PaymentMethod getMethod() {
			return method;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("acc_id=").append(getAccId()).append(", ").append("extra_part=").append(getExtraPart()).append(", ").append("item_id=").append(getItemId()).append(", ").append("mp_op_id=").append(getMpOpId())
					.append(", ").append("name=").append(getName()).append(", ").append("seller_op_id=").append(getSellerOpId()).append(", ").append("price=").append(getPrice()).append(", ").append("shipping_amount=")
					.append(getShippingAmount()).append(", ").append("total_amount=").append(getTotalAmount()).append(", ").append("payment_method=").append(getMethod()).append(", ").append("status=")
					.append(getStatus()).append(", ");
			return sb.toString();
		}

	}

	private static Logger logger = Logger.getLogger(Payments.class);

	enum Action {
		LOAD, NOTIFY;

		public static Action getFromContext(HttpServletRequest context) {
			String action = context.getRequestURI().replace(context.getContextPath() + "/payments/", "").toUpperCase();
			return Action.valueOf(action);
		}
	}

	@Override
	public void init() throws ServletException {
		super.init();
		dateFormat = new SimpleDateFormat("yyyy/MM/dd Z");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-3:00"));

	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Action action = Action.getFromContext(request);
		switch (action) {
		case NOTIFY:
			try {
				update(request, response);
			} catch (LdException e) {
				throw new ServletException(e);
			}
			break;
		case LOAD:
			load(request, response);
			break;
		}
	}

	private void load(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			FileUtil fileUtil = getComponent(FileUtil.class);
			Planner planner = (Planner) getEntity(request, FileEntity.Types.PLANNER);

			if (supportsCompression(request)) {
				response.setHeader("Content-Encoding", "gzip");
			}
			response.setHeader("Expires", "0");

			fileUtil.streamOut(planner.loadPayment(supportsCompression(request)), response.getOutputStream());
		} catch (LdException e) {
			throw new ServletException(e);
		}

	}

	private void update(HttpServletRequest request, HttpServletResponse response) throws LdException {
		Payment pmt = new Payment(request);
		logger.info("PAYMENT NOTIFY: " + pmt.toString());
		try {
			Node payment = getPaymentInfo(request);
			String extraInfo = getExtraInfo(payment);
			if (extraInfo.startsWith("STANDALONEEVENT-")) {
				updateEventPayment(payment, extraInfo);
			} else {
				updatePlanerPayment(payment, extraInfo);
			}
		} catch (IOException e) {
			throw new LdException("Can't retrieve payment info from: "
					+ pmt.toString(), e);
		} catch (XPathExpressionException e) {
			throw new LdException("Can't retrieve extra-part from payment", e);
		} catch (TransformerException e) {
			throw new LdException("Can't retrieve extra-part from payment", e);
		}
	}
	private String getExtraInfo(Node payment) throws XPathExpressionException, TransformerException {
		XPath xPath = getComponent(XMLUtils.class).getXpath();
		String extraInfo = (String) xPath.evaluate(EXTRA_PART_XPATH, payment, XPathConstants.STRING);
		return extraInfo;
	}

	private void updatePlanerPayment(Node payment, String extraPart) throws LdException {
		String plannerId = extraPart.split("-")[0];
		Admin admin = getComponent(Admin.class);
		try {
			Planner planner = admin.getPlanner(plannerId);
			try {
				planner.paymentUpdate(new DOMSource(payment));
				String expirationDate = getLastPaymentExpiration(planner);
				admin.permissionUpdate(plannerId, expirationDate);
			} catch (Exception e) {
				throw new LdException("Can't update payment info of planner " + plannerId, e);
			}
		} catch (EntityException e) {
			logger.info("Reported payment update over unexistent planner " + plannerId);
		}
	}

	private void updateEventPayment(Node payment, String extraPart) throws LdException {
		String eventId = extraPart.split("-")[1];
		XPath xPath = getComponent(XMLUtils.class).getXpath();
		try {
			boolean isPaymentAccpted = (Boolean) xPath.evaluate(PAYMENT_ACCEPTED_XPATH, payment, XPathConstants.BOOLEAN);
			if(isPaymentAccpted) {
				Admin admin = getComponent(Admin.class);
				try {
					Planner defaultPlanner = admin.getPlanner("0");
					try {
						defaultPlanner.permissionUpdate(eventId, "3M");
					} catch (Exception e) {
						throw new LdException("Can't update payment info of stand alone event id " + eventId, e);
					}
				} catch (EntityException e) {
					logger.info("Reported payment update over unexistent stand alone event " + eventId);
				}
			}
		} catch (XPathExpressionException e1) {
			throw new LdException(e1);
		}
	}

	private String getLastPaymentExpiration(Planner planner) throws XPathExpressionException, IOException {
		XPath xpath = getComponent(XMLUtils.class).getXpath();
		NodeList periods = (NodeList) xpath.evaluate("//payment[status/state = 'A' or status/state = 'B']/period/to",
				new InputSource(planner.loadPayment(false)), XPathConstants.NODESET);
		List<Date> dates = new ArrayList<Date>();
		for (int i = 0; i < periods.getLength(); i++) {
			try {
				dates.add(getDateFormat().parse(periods.item(i).getTextContent()));
			} catch (DOMException e) {
				logger.error(e);
			} catch (ParseException e) {
				logger.error(e);
			}
		}
		Date last = Collections.max(dates);
		Calendar cal = Calendar.getInstance();
		cal.setTime(last);
		cal.add(Calendar.DAY_OF_MONTH, 1);
		return getDateFormat().format(cal.getTime());
	}

	private Node getPaymentInfo(HttpServletRequest request) throws IOException {
		Config conf = getComponent(Config.class);
		String sellerOpId = request.getParameter("seller_op_id");
		String mpOpId = request.getParameter("mp_op_id");

		Map<String, String> params = new HashMap<String, String>();
		params.put("acc_id", conf.get("mp.accountId"));
		params.put("sonda_key", conf.get("mp.sonda.key"));
		params.put("mp_op_id", mpOpId);
		params.put("seller_op_id", sellerOpId);

		InputStream response = sendPostRequest(conf.get("mp.sonda.url"), params);
		Document doc = null;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(response);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} 
		return doc.getDocumentElement();
	}

	public InputStream sendPostRequest(String location, Map<String, String> params) throws IOException {
		// Send the request
		URL url = new URL(location);
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setDoOutput(true);
		OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

		// write parameters
		for (Map.Entry<String, String> entry : params.entrySet()) {
			writer.append(entry.getKey()).append('=').append(entry.getValue()).append('&');
		}
		writer.close();

		// Get the response
		return conn.getInputStream();
	}

	protected boolean supportsCompression(HttpServletRequest request) {
		String encoding = request.getHeader("Accept-Encoding");
		return encoding.contains("gzip");
	}
	
	private DateFormat getDateFormat() {
		return (DateFormat) dateFormat.clone();
	}

}
