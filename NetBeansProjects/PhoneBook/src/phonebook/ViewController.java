package phonebook;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class ViewController implements Initializable {
    
    @FXML
    TableView table;
    @FXML
    TextField inputLastname;
    @FXML
    TextField inputFirstname;
    @FXML
    TextField inputEmail;
    @FXML
    Button addNewContactButton;
    @FXML
    StackPane menuPane;
    @FXML
    Pane contactPane;
    @FXML
    Pane exportPane;
    @FXML
    TextField inputExportName;
    @FXML
    Button exportButton;
    
    private final String MENU_CONTACTS = "Contacts";
    private final String MENU_LIST = "List";
    private final String MENU_EXPORT = "Export";
    private final String MENU_EXIT = "Exit";
    
    private final ObservableList<Person> data = FXCollections.observableArrayList(new Person("Gyula", "Szabo", "szabo_gyula@gmail.com"), new Person("Monika", "Prelcsec", "monika@hotmail.com"));
    
    @FXML
    public void addContact(ActionEvent event) {
        String email = inputEmail.getText();
        if (email.length() > 3 && email.contains("@") && email.contains(".")) {
        data.add(new Person(inputLastname.getText(), inputFirstname.getText(), email));
        inputLastname.clear();
        inputFirstname.clear();
        inputEmail.clear();
        }
    }
    
    @FXML
    public void exportList(ActionEvent event) {
        String fileName = inputExportName.getText();
        fileName = fileName.replace("\\s+", "");
        if (fileName != null && !fileName.equals("")) {
            PdfGenerator pdfCreator = new PdfGenerator();
            pdfCreator.pdfGenerator(fileName, data);
        }
    }    
    
    public void setTableData() {
        TableColumn lastNameCol = new TableColumn("Lastname");
        lastNameCol.setMinWidth(100);
        lastNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        lastNameCol.setCellValueFactory(new PropertyValueFactory<Person, String>("lastName"));
        
        lastNameCol.setOnEditCommit(
          new EventHandler<TableColumn.CellEditEvent<Person, String>>() {
              @Override
              public void handle(TableColumn.CellEditEvent<Person, String> t) {
                  ((Person) t.getTableView().getItems().get(
                  t.getTablePosition().getRow())
                  ).setLastName(t.getNewValue());
              }
          }
        );
        
        TableColumn firstNameCol = new TableColumn("Firstname");
        firstNameCol.setMinWidth(100);
        firstNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        firstNameCol.setCellValueFactory(new PropertyValueFactory<Person, String>("firstName"));
        
        firstNameCol.setOnEditCommit(
          new EventHandler<TableColumn.CellEditEvent<Person, String>>() {
              @Override
              public void handle(TableColumn.CellEditEvent<Person, String> t) {
                  ((Person) t.getTableView().getItems().get(
                  t.getTablePosition().getRow())
                  ).setFirstName(t.getNewValue());
              }
          }
        );
        
        TableColumn emailCol = new TableColumn("E-mail");
        emailCol.setMinWidth(200);
        emailCol.setCellFactory(TextFieldTableCell.forTableColumn());
        emailCol.setCellValueFactory(new PropertyValueFactory<Person, String>("email"));
        
        emailCol.setOnEditCommit(
          new EventHandler<TableColumn.CellEditEvent<Person, String>>() {
              @Override
              public void handle(TableColumn.CellEditEvent<Person, String> t) {
                  ((Person) t.getTableView().getItems().get(
                  t.getTablePosition().getRow())
                  ).setEmail(t.getNewValue());
              }
          }
        );
        
        table.getColumns().addAll(lastNameCol, firstNameCol, emailCol);
        table.setItems(data);
    }
    
    public void setMenuData() {
        TreeItem<String> treeItemRoot1 = new TreeItem<>("Menu");
        TreeView<String> treeView = new TreeView<>(treeItemRoot1);
        treeView.setShowRoot(false);
        
        TreeItem<String> nodeItemA = new TreeItem<>(MENU_CONTACTS);
        TreeItem<String> nodeItemB = new TreeItem<>(MENU_EXIT);
        
        nodeItemA.setExpanded(true);
        
        Node contactsNode = new ImageView(new Image(getClass().getResourceAsStream("/contacts.png")));
        Node exportNode = new ImageView(new Image(getClass().getResourceAsStream("/export.png")));
        
        TreeItem<String> nodeItemA1 = new TreeItem<>(MENU_LIST, contactsNode);
        TreeItem<String> nodeItemA2 = new TreeItem<>(MENU_EXPORT, exportNode);
        
        nodeItemA.getChildren().addAll(nodeItemA1, nodeItemA2);
        treeItemRoot1.getChildren().addAll(nodeItemA, nodeItemB);
        
        menuPane.getChildren().add(treeView);
        
        treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                TreeItem<String> selectedItem = (TreeItem<String>) newValue;
                String selectedMenu = selectedItem.getValue();
                
                if (null != selectedMenu) {
                    switch (selectedMenu) {
                        case MENU_CONTACTS:
                            try {
                                selectedItem.setExpanded(true);
                            } catch (Exception ex) {}
                            break;
                        case MENU_LIST:
                            contactPane.setVisible(true);
                            exportPane.setVisible(false);
                            break;
                        case MENU_EXPORT:
                            contactPane.setVisible(false);
                            exportPane.setVisible(true);
                            break;    
                        case MENU_EXIT:
                            System.exit(0);
                            break;
                    }
                }
            }
        });
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setTableData();
        setMenuData();
        
    }    
}
