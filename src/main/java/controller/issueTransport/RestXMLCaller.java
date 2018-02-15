package controller.issueTransport;

import org.w3c.dom.Document;

public interface RestXMLCaller {
    Document callRestDOM(String module, String operation_name, String input_data);
}
