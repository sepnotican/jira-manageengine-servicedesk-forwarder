package controller.issueTransport;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import core.Settings;
import core.Tools;
import entity.TaskModel;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by muzafar on 2/28/17.
 */
@Singleton
public class ServiceDeskHandler {

    @Inject
    private RestXMLCallerImpl restXMLCallerImpl;

    public ArrayList<TaskModel> get_requests(int from, int limit) {

        ArrayList<TaskModel> result_ = new ArrayList<TaskModel>();

        UnheadedXMLBuilder xmlBuilder = new UnheadedXMLBuilder();
        Map<String, Object> paramsSet = new HashMap<String, Object>();

        paramsSet.put("from", Integer.toString(from));
        paramsSet.put("limit", Integer.toString(limit));
        final String filterName = Settings.getSettings().getServicedeskForwardViewName();
        final String idByName = getFilterIdByName(filterName);
        if (idByName == null) {
            Tools.logger.error("Resolving filter ID by name failed. filterName = ".concat(filterName));
            return result_;
        }

        paramsSet.put("filterby", idByName);

        String xml = xmlBuilder.makeTag("Details", xmlBuilder.processParams(paramsSet));

        Document document = restXMLCallerImpl.callRestDOM("sdpapi/request", "GET_REQUESTS", xml);

        if (document == null) {
            Tools.logger.error("Document is empty. xml= " + xml);
            return result_;
        }
        // Получаем корневой элемент
        Node root = document.getDocumentElement();

        NodeList api_children = root.getChildNodes();
        NodeList responses_children = api_children.item(1).getChildNodes();
        NodeList operation_children = responses_children.item(1).getChildNodes();

        for (int i = 0; i < operation_children.getLength(); i++) {
            Node operation_node = operation_children.item(i);
            if (operation_node.getNodeType() == Node.TEXT_NODE) continue;
            if (operation_node.getNodeName().equals("Details")) {
                NodeList record_children = operation_node.getChildNodes();
                for (int j = 0; j < record_children.getLength(); j++) {
                    Node record = record_children.item(j);
                    if (record.getNodeType() == Node.TEXT_NODE) continue;

                    NamedNodeMap attribs = record.getAttributes();
                    if (attribs.getLength() > 0) {
                        Node attr_URI = attribs.getNamedItem("URI");
                        if (attr_URI == null) continue;
                    }
                    //else -- create object
                    TaskModel task = new TaskModel();
                    //task.setDate(new Date(0L));
                    //System.out.println(attr_URI.getNodeValue());

                    NodeList param_children = record.getChildNodes();
                    for (int k = 0; k < param_children.getLength(); k++) {
                        Node param = param_children.item(k);
                        if (param.getNodeType() == Node.TEXT_NODE) continue;
                        NodeList paramContent = param.getChildNodes();

                        for (int l = 0; l < paramContent.getLength(); l++) {
                            Node name = paramContent.item(1);
                            Node value = paramContent.item(3);

                            switch (name.getTextContent().trim()) {
                                case "workorderid":
                                    try {
                                        task.setId_sd(Integer.parseInt(value.getTextContent()));
                                    } catch (NumberFormatException e) {
                                        Tools.logger.error(e.getMessage());
                                        continue;
                                    }
                                    break;
                                case "requester":
                                    task.setRequester(value.getTextContent());
                                    break;
                                default:
                                    break;
                            }
                        }

                    } //param
                    result_.add(task);
                } //record
            } //detail
        } //operation
        return result_;
    }

    public void fillIssueByID(TaskModel taskModel) {

        UnheadedXMLBuilder xmlBuilder = new UnheadedXMLBuilder();
        Map<String, Object> paramsSet = new HashMap<String, Object>();
        paramsSet.put("limit", 1);

        String xml = xmlBuilder.makeTag("Details", xmlBuilder.processParams(paramsSet));
        Document document = restXMLCallerImpl.callRestDOM("sdpapi/request".concat("/").concat(Integer.toString(taskModel.getId_sd())), "GET_REQUEST", xml);

        if (document == null) {
            Tools.logger.error("Can't get document (null). taskModel = ".concat(String.valueOf(taskModel.getId_sd())));
            return;
        }
        // Получаем корневой элемент
        Node root = document.getDocumentElement();

        NodeList api_children = root.getChildNodes();
        NodeList responses_children = api_children.item(1).getChildNodes();
        NodeList operation_children = responses_children.item(1).getChildNodes();

        for (int i = 0; i < operation_children.getLength(); i++) {
            Node operation_node = operation_children.item(i);
            if (operation_node.getNodeType() == Node.TEXT_NODE) continue;
            if (operation_node.getNodeName().equals("Details")) {
                NodeList record_children = operation_node.getChildNodes(); //details
                for (int j = 0; j < record_children.getLength(); j++) {
                    Node record = record_children.item(j);
                    if (record.getNodeType() == Node.TEXT_NODE) continue;
                    NodeList param_children = record.getChildNodes();

                    Node name = param_children.item(1);
                    Node value = param_children.item(3);

                    if (name == null || value == null) continue;
                    String s = name.getTextContent().trim();
                    switch (s) {
                        case "technician_loginname":
                            taskModel.setAssingnee(value.getTextContent());
                            break;
                        case "description":
                            taskModel.setDescription(formatHtmlContent(value.getTextContent()));
                            break;
                        case "subject":
                            taskModel.setSummary(value.getTextContent());
                            break;
                        default:
                            break;
                    }

                } //record
            } //detail
        } //operation
    }

    public String getFilterIdByName(String filterName) {

        Document document = restXMLCallerImpl.callRestDOM("sdpapi/request", "GET_REQUEST_FILTERS", null);

        if (document == null) {
            Tools.logger.error("Can't get document (null). Filtername = ".concat(filterName));
            return null;
        }
        // Получаем корневой элемент
        Node root = document.getDocumentElement();

        NodeList operation_tree = root.getChildNodes();
        NodeList detail_tree = operation_tree.item(3).getChildNodes();
        NodeList filters_tree = detail_tree.item(1).getChildNodes();

        for (int i = 0; i < filters_tree.getLength(); i++) {
            Node operation_node = filters_tree.item(i);
            if (operation_node.getNodeType() == Node.TEXT_NODE) continue;
            if (operation_node.getNodeName().equals("parameter")) {
                NodeList record_children = operation_node.getChildNodes(); //details


                Node name = record_children.item(1);
                Node value = record_children.item(3);

                if (name == null || value == null) continue;
                String sName = name.getTextContent().trim();
                String sValue = value.getTextContent().trim();
                if (sValue.equals(filterName)) {
                    return sName;
                } //
            } //
        }
        return null;
    }

    public String formatHtmlContent(String income) {
        char c;
        StringBuilder sb = new StringBuilder();
        int len = income.length();

        boolean tagOpen = false;
        boolean tagImgOpened = false;
        for (int i = 0; i < len; i++) {
            c = income.charAt(i);

            //linebreak
            if (c == '<' && i + 2 <= len &&
                    'p' == income.charAt(i + 1) &&
                    '>' == income.charAt(i + 2)) {
                sb.append('\n');
                i += 2;
                continue;
            }

            //image
            if (c == '<' && i + 3 <= len &&
                    'i' == income.charAt(i + 1) &&
                    'm' == income.charAt(i + 2) &&
                    'g' == income.charAt(i + 3)) {
                tagImgOpened = true;
                tagOpen = true;
                i += 3;
                continue;
            }

            if (tagImgOpened) {
                if (c == 's' && i + 3 <= len &&
                        'r' == income.charAt(i + 1) &&
                        'c' == income.charAt(i + 2) &&
                        '=' == income.charAt(i + 3)) {
                    i += 5;
                    sb.append("IMAGE: [").append(Settings.getSettings().getServiceDeskHttpURL()).append('/');
                    for (; '"' != income.charAt(i); i++) {
                        sb.append(income.charAt(i));
                    }
                    sb.append(']');
                    tagImgOpened = false;
                    continue;
                }
            }

            //other tags
            if (c == '<')
                tagOpen = true;
            else if (c == '>') {
                tagOpen = false;
                tagImgOpened = false;
                continue;
            }

            if (tagOpen)
                continue;

            sb.append(c);
        }
        return sb.toString();
    }

    public boolean completeRequest(int sd_id, String resolution, String assingneeName) {
        return addResolution(sd_id, resolution, assingneeName)
                && closeRequest(sd_id);
    }

    private boolean addResolution(int sd_id, String resolution, String assingneeName) {
        UnheadedXMLBuilder xmlBuilder = new UnheadedXMLBuilder();
        String xml = xmlBuilder.makeTag("Details",
                xmlBuilder.makeTag("resolution",
                        xmlBuilder.makeTag("resolutiontext",
                                replaceIllegalChars(resolution + "\nРешение предоставлено:" + assingneeName))));

        final String url = String.format("sdpapi/request/%d/resolution", sd_id);
        Document document = restXMLCallerImpl.callRestDOM(url, "ADD_RESOLUTION", xml);

//        ((DeferredDocumentImpl) document).getNodeValue(11)
        if (document == null) {
            Tools.logger.error("Document is empty. xml= " + xml);
            return false;
        }

        // Получаем корневой элемент
        Node root = document.getDocumentElement();

        NodeList rootChildNodes = root.getChildNodes();
        NodeList childNodes = rootChildNodes.item(1).getChildNodes();

        String status = "", message = "";

        for (int i = 0; i < childNodes.getLength(); i++) {
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node resultNode = childNodes.item(j);
                if (resultNode.getNodeType() == Node.TEXT_NODE) continue;
                if (resultNode.getNodeName().equals("status"))
                    status = resultNode.getTextContent();
                else if (resultNode.getNodeName().equals("message"))
                    message = resultNode.getTextContent();
            }
        }
        if (status.equals("Success")) {
            Tools.logger.info("Resolution added. SD ID: " + sd_id);
            return true;
        } else {
            Tools.logger.error("Error while adding resolution Service desk request. SD ID:" + sd_id
                    + "\nMessage: " + message);
            return false;
        }
    }

    private String replaceIllegalChars(String resolution) {
        return resolution
                .replace("\n", "&lt;br /&gt;")
                .replace("\r", "")
                ;
    }

    public boolean closeRequest(int sd_id) {
        ArrayList<TaskModel> result_ = new ArrayList<TaskModel>();

        final String url = String.format("sdpapi/request/%d/", sd_id);
        Document document = restXMLCallerImpl.callRestDOM(url, "CLOSE_REQUEST", null);

        if (document == null) {
            Tools.logger.error("Document is empty. SD ID: " + sd_id);
            return false;
        }

        // Получаем корневой элемент
        Node root = document.getDocumentElement();

        NodeList rootChildNodes = root.getChildNodes();
        NodeList childNodes = rootChildNodes.item(1).getChildNodes();
        NodeList operationChildren = childNodes.item(1).getChildNodes();
        String status = "", message = "";

        for (int i = 0; i < operationChildren.getLength(); i++) {
            Node operationNode = operationChildren.item(i);
            if (operationNode.getNodeType() == Node.TEXT_NODE) continue;
            if (operationNode.getNodeName().equals("result")) {
                NodeList resultList = operationNode.getChildNodes();
                for (int j = 0; j < resultList.getLength(); j++) {
                    Node resultNode = resultList.item(j);
                    if (resultNode.getNodeType() == Node.TEXT_NODE) continue;
                    if (resultNode.getNodeName().equals("status"))
                        status = resultNode.getTextContent();
                    if (resultNode.getNodeName().equals("message"))
                        message = resultNode.getTextContent();
                }
            }
        }
        if (!status.equals("Success")) {
            Tools.logger.error("Error while closing Service desk request. SD ID:" + sd_id
                    + "\nMessage: " + message);
            return false;
        }

        return true;
    }
}
