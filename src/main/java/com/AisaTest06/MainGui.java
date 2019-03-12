package com.AisaTest06;

import com.AisaTest06.dao.DAO;
import com.AisaTest06.model.Company;
import com.AisaTest06.model.Employee;
import com.AisaTest06.view.*;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.HasValue;
import com.vaadin.server.*;
import com.vaadin.ui.*;

import javax.servlet.annotation.WebServlet;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

@Theme("mytheme")


public class MainGui extends UI {

    private static Logger logger = Logger.getLogger(MainGui.class.getName());
    DAO dao = new DAO();


    @Override
    protected void init(VaadinRequest vaadinRequest) {
        Grids grids = new Grids();
        Lists lists = new Lists();
        UpButtons upButton = new UpButtons();



        dao.dataSource();


        VerticalLayout mainLayout = new VerticalLayout();
        HorizontalLayout headLayout = new HorizontalLayout();
        headLayout.setMargin(false);
        headLayout.setSpacing(true);


        VerticalLayout mainWindowLayout = new VerticalLayout();
        HorizontalLayout tab1 = new HorizontalLayout();
        HorizontalLayout tab2 = new HorizontalLayout();

        TabSheet tabSheet = new TabSheet();
        tabSheet.addTab(tab1, "Компании");
        tabSheet.addTab(tab2, "Сотрудники");
        tabSheet.setSelectedTab(tab1);

        Button addButton = upButton.addButton("Добавить");
        Button deleteButton = upButton.deleteButton("Удалить");
        Button editButton = upButton.editButton("Редактировать");

        TextField search = new TextField();
        search.setPlaceholder("поиск");
        search.setSizeFull();

        Grid<Employee> employeeGrid = grids.gridEmployees();
        Grid<Company> companyGrid = grids.gridCompanies();
        employeeGrid.setSizeFull();
        companyGrid.setSizeFull();


        ComboBox<Company> selectAllCompanies = new ComboBox<>("Выбрать компанию");
        ComboBox<Employee> selectAllEmployees = new ComboBox<>("Выбрать сотрудника");

        selectAllCompanies.setEmptySelectionAllowed(false);
        selectAllEmployees.setEmptySelectionAllowed(false);

        List<Company> companyIds = lists.fillingListCompany();
        List<Employee> employeeIds = lists.fillingListEmployee();

        final int[] CompanyIdArr = {0};
        final int[] employeeIdArr = {0};
        final String[] fullNameArr = {""};
        final String[] dateArr = {""};
        final String[] emailArr = {""};
        final String[] textFieldNameArr = {""};
        final String[] companyNameArr = {""};
        final String[] NIPArr = {""};
        final String[] AddressArr = {""};
        final String[] PhoneArr = {""};

        headLayout.addComponents(addButton, editButton, deleteButton, search);
        mainLayout.addComponent(headLayout);
        mainLayout.addComponent(tabSheet);
        companyGrid.setItems(dao.selectAllCompanies());
        mainLayout.addComponent(companyGrid);
        setContent(mainLayout);

        TextField name = new TextField("Название компании");
        TextField nip = new TextField("ИНН");
        TextField address = new TextField("Адрес");
        TextField phone = new TextField("телефон");

        TextField fullName = new TextField("ФИО");
        DateField dateField = new DateField("Дата рождения");
        TextField email = new TextField("Email");

        search.addValueChangeListener(e ->{
            if (tabSheet.getSelectedTab().equals(tab1)){
                companyGrid.setItems(dao.searchAllCompanies(search.getValue()));
                mainLayout.addComponent(companyGrid);
                mainLayout.removeComponent(employeeGrid);
                logger.info("Выбран tab1 с search "+ search.getValue());
            }
            else if (tabSheet.getSelectedTab().equals(tab2)){
                employeeGrid.setItems(dao.searchAllEmployees(search.getValue()));
                mainLayout.addComponent(employeeGrid);
                mainLayout.removeComponent(companyGrid);
                logger.info("Выбран tab2 с search "+ search.getValue());
            }
        }) ;

        selectAllEmployees.addValueChangeListener(event -> {
            Employee employee = event.getValue();
            employeeIdArr[0] = employee.getEmployeeId();
            logger.info("Выбран сотрудник в combobox "+employee.getFullname());

        });

        selectAllCompanies.addValueChangeListener(event -> {
            Company company = event.getValue();
            CompanyIdArr[0] = company.getCompanyId();
            logger.info("Выбрана компания в combobox "+company.getName());
        });

        tabSheet.addSelectedTabChangeListener(
                (TabSheet.SelectedTabChangeListener) e -> {
                    if (tabSheet.getSelectedTab().equals(tab1)) {
                        companyGrid.setItems(dao.selectAllCompanies());
                        mainLayout.addComponent(companyGrid);
                        mainLayout.removeComponent(employeeGrid);

                        logger.info("Выбран tab1" );

                    } else if (tabSheet.getSelectedTab().equals(tab2)){
                        employeeGrid.setItems(dao.selectAllEmployees());
                        mainLayout.addComponent(employeeGrid);
                        mainLayout.removeComponent(companyGrid);

                        logger.info("Выбран tab2");

                    }
                });


        nip.addValueChangeListener(event->{
            if (event.getValue().length()!=12&&event.getValue().matches("\\d+")){
                nip.setComponentError(new UserError("Должно быть 12 цифр"));
            }
            else {
                nip.setComponentError(null);
                Notification.show(event.getValue());
            }
        });

        phone.addValueChangeListener(event->{
            if (!event.getValue().matches("\\d+")){
                phone.setComponentError(new UserError("Должны быть цифры"));
            }
            else {
                phone.setComponentError(null);
                Notification.show(event.getValue());
            }
        });
        email.addValueChangeListener(event->{
             String EMAIL_PATTERN =
                    "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
                            "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
            if (!event.getValue().matches(EMAIL_PATTERN)){
                email.setComponentError(new UserError("Нужен email"));
            }
            else {
                email.setComponentError(null);
                Notification.show(event.getValue());
            }

        });


        addButton.addClickListener(clickEvent -> {

            if (tabSheet.getSelectedTab().equals(tab1)) {

                AddCompany addComWindow = new AddCompany();

                Button addCompany = new Button("Добавить компанию");

                events(companyNameArr, NIPArr, AddressArr, PhoneArr, name, nip, address, phone);

                addCompany.addClickListener((Button.ClickListener) clickEvent6 -> {
                    Page.getCurrent().reload();
                    addComWindow.close();
                    mainWindowLayout.removeAllComponents();
                    tabSheet.setSelectedTab(tab2);
                    Company company = null;
                    try {

                    company= new Company(companyNameArr[0], Long.parseLong(NIPArr[0])
                            , AddressArr[0], Long.parseLong(PhoneArr[0]));

                    }
                    catch (NumberFormatException ex){
                       logger.warning("Неверные данные компании "+ ex);
                    }
                    if (companyNameArr[0].isEmpty() || NIPArr[0].isEmpty() ||
                            AddressArr[0].isEmpty() || PhoneArr[0].isEmpty()) {

                        Notification.show("Ошибка " + fullNameArr[0] );

                    } else {
                        dao.insertCompany(company);
                        Notification.show(companyNameArr[0] + "Ок");
                    }
                });

                mainWindowLayout.addComponents(name, nip, address, phone, addCompany);
                addComWindow.addCloseListener((Window.CloseListener)
                        closeEvent -> mainWindowLayout.removeAllComponents());


                addComWindow.setContent(mainWindowLayout);
                addWindow(addComWindow);
            } else if (tabSheet.getSelectedTab().equals(tab2)) {


                AddEmployee additionWindow = new AddEmployee();

                Button addEmployee = new Button("Добавить сотрудника");

                fullName.setRequiredIndicatorVisible(true);
                dateField.setRequiredIndicatorVisible(true);
                email.setRequiredIndicatorVisible(true);

                mainWindowLayout.addComponents(fullName, dateField, email,
                        selectAllCompanies, addEmployee);


                fullName.addValueChangeListener(
                        (HasValue.ValueChangeListener<String>) valueChangeEvent ->
                                fullNameArr[0] = valueChangeEvent.getValue());

                dateField.addValueChangeListener(valueChangeEvent -> {
                    LocalDate date = valueChangeEvent.getValue();
                    dateArr[0] = date.toString();
                });

                email.addValueChangeListener(
                        (HasValue.ValueChangeListener<String>) valueChangeEvent ->
                                emailArr[0] = valueChangeEvent.getValue());


                selectAllCompanies.setItems(companyIds);
                selectAllCompanies.setItemCaptionGenerator(Company::getName);

                additionWindow.addCloseListener((Window.CloseListener)
                        closeEvent -> mainWindowLayout.removeAllComponents());


                addEmployee.addClickListener((Button.ClickListener) clickEvent1 -> {
                    Page.getCurrent().reload();
                    additionWindow.close();
                    mainWindowLayout.removeAllComponents();
                    tabSheet.setSelectedTab(tab2);
                    Employee employee = new Employee(fullNameArr[0], dateArr[0]
                            , emailArr[0], CompanyIdArr[0]);

                    if (fullNameArr[0].isEmpty() || dateArr[0].isEmpty() || emailArr[0].isEmpty()) {
                        Notification.show("Ошибка " + fullNameArr[0] );

                    } else {
                        dao.insertEmployee(employee);
                        Notification.show(fullNameArr[0] + "Ок");

                    }
                });

                additionWindow.setContent(mainWindowLayout);
                addWindow(additionWindow);
            }
        });


        deleteButton.addClickListener((Button.ClickListener) clickEvent -> {
            if (tabSheet.getSelectedTab().equals(tab1)) {
                DeleteCompany deleteComWindow = new DeleteCompany();
                Button deleteCompany = new Button("Удалить компанию");

                selectAllCompanies.setItems(companyIds);
                selectAllCompanies.setItemCaptionGenerator(Company::getName);

                mainWindowLayout.addComponents(selectAllCompanies, deleteCompany);

                deleteComWindow.addCloseListener((Window.CloseListener)
                        closeEvent -> mainWindowLayout.removeAllComponents());

                deleteCompany.addClickListener((Button.ClickListener) clickEvent12 -> {
                    Page.getCurrent().reload();
                    deleteComWindow.close();
                    mainWindowLayout.removeAllComponents();
                    tabSheet.setSelectedTab(tab2);
                    Company company = new Company(CompanyIdArr[0]);
                    dao.deleteCompany(company);
                    Notification.show(CompanyIdArr[0] + "Ок");

                });

                deleteComWindow.setContent(mainWindowLayout);
                addWindow(deleteComWindow);

            } else if (tabSheet.getSelectedTab().equals(tab2)) {
                DeleteEmployee deleteWindow = new DeleteEmployee();
                Button deleteEmployee = new Button("Удалить сотрудника");

                mainWindowLayout.addComponents(selectAllEmployees, deleteEmployee);

                selectAllEmployees.setItems(employeeIds);
                selectAllEmployees.setItemCaptionGenerator(Employee::getFullname);

                deleteWindow.addCloseListener((Window.CloseListener)
                        closeEvent -> mainWindowLayout.removeAllComponents());

                deleteEmployee.addClickListener((Button.ClickListener) clickEvent12 -> {
                    Notification.show(employeeIdArr[0] + "Ок");
                    Page.getCurrent().reload();
                    deleteWindow.close();
                    mainWindowLayout.removeAllComponents();
                    tabSheet.setSelectedTab(tab1);
                    Employee employee = new Employee(employeeIdArr[0]);
                    dao.deleteEmployee(employee);


                });
                deleteWindow.setContent(mainWindowLayout);
                addWindow(deleteWindow);
            }
        });


        editButton.addClickListener((Button.ClickListener) clickEvent -> {
            if (tabSheet.getSelectedTab().equals(tab1)) {
                EditCompany editComWindow = new EditCompany();

                Button editCompany = new Button("Редактировать компанию");

                events(companyNameArr, NIPArr, AddressArr, PhoneArr, name, nip, address, phone);

                selectAllCompanies.setItems(companyIds);
                selectAllCompanies.setItemCaptionGenerator(Company::getName);

                mainWindowLayout.addComponents(selectAllCompanies, name, nip, address, phone, editCompany);

                editComWindow.addCloseListener((Window.CloseListener)
                        closeEvent -> mainWindowLayout.removeAllComponents());

                editCompany.addClickListener((Button.ClickListener) clickEvent15 -> {
                    Page.getCurrent().reload();
                    editComWindow.close();
                    mainWindowLayout.removeAllComponents();
                    tabSheet.setSelectedTab(tab2);

                    Company company = null;
                    try {

                    company= new Company(CompanyIdArr[0], companyNameArr[0], Long.parseLong(NIPArr[0])
                            , AddressArr[0], Long.parseLong(PhoneArr[0]));
                    }
                    catch (NumberFormatException ex){
                        logger.warning("Неверная редакция компании "+ ex);
                    }

                    if (companyNameArr[0].isEmpty() || NIPArr[0].isEmpty() ||
                            AddressArr[0].isEmpty() || PhoneArr[0].isEmpty()) {
                        Notification.show("Ошибка " + CompanyIdArr[0] );

                    } else {
                        dao.editCompany(company);
                        Notification.show(CompanyIdArr[0] + "Ок");
                    }
                });

                editComWindow.setContent(mainWindowLayout);
                addWindow(editComWindow);


            } else if (tabSheet.getSelectedTab().equals(tab2)) {
                EditEmployee editWindow = new EditEmployee();

                Button editEmployee = new Button("Редактировать сотрудника");

                fullName.setRequiredIndicatorVisible(true);
                dateField.setRequiredIndicatorVisible(true);
                email.setRequiredIndicatorVisible(true);


                mainWindowLayout.addComponents(selectAllEmployees, fullName, dateField,
                        email, selectAllCompanies, editEmployee);

                dateField.addValueChangeListener(
                        (HasValue.ValueChangeListener<LocalDate>) valueChangeEvent -> {
                            LocalDate date = valueChangeEvent.getValue();
                            dateArr[0] = date.toString();
                        });

                email.addValueChangeListener(
                        (HasValue.ValueChangeListener<String>) valueChangeEvent ->
                                emailArr[0] = valueChangeEvent.getValue());

                fullName.addValueChangeListener(
                        (HasValue.ValueChangeListener<String>) valueChangeEvent ->
                                textFieldNameArr[0] = valueChangeEvent.getValue());

                selectAllCompanies.setItems(companyIds);
                selectAllCompanies.setItemCaptionGenerator(Company::getName);
                selectAllEmployees.setItems(employeeIds);
                selectAllEmployees.setItemCaptionGenerator(Employee::getFullname);

                editWindow.addCloseListener((Window.CloseListener) closeEvent ->
                        mainWindowLayout.removeAllComponents());

                editEmployee.addClickListener((Button.ClickListener) clickEvent13 -> {
                    Page.getCurrent().reload();
                    editWindow.close();
                    mainWindowLayout.removeAllComponents();
                    tabSheet.setSelectedTab(tab1);
                    Employee employee =null;
                    try {
                        employee = new Employee(
                                employeeIdArr[0], textFieldNameArr[0], dateArr[0], emailArr[0], CompanyIdArr[0]);
                    }
                    catch (NumberFormatException ex){
                        logger.warning("Неверная редакция сотрудника "+ ex);
                    }


                    if (textFieldNameArr[0].isEmpty() || dateArr[0].isEmpty() || emailArr[0].isEmpty()) {
                        Notification.show("Ошибка " + employeeIdArr[0] );

                    } else {
                        dao.editEmployee(employee);
                        Notification.show(employeeIdArr[0] + " Ок");
                    }
                });

                editWindow.setContent(mainWindowLayout);
                addWindow(editWindow);
            }
        });
    }

    private void events(String[] companyNameArr, String[] NIPArr, String[] addressArr, String[] phoneArr,
                        TextField name, TextField nip, TextField address, TextField phone) {

        name.setRequiredIndicatorVisible(true);
        nip.setRequiredIndicatorVisible(true);
        address.setRequiredIndicatorVisible(true);
        phone.setRequiredIndicatorVisible(true);

        if (!dao.checkCompanyByName(companyNameArr[0])){



        name.addValueChangeListener(valueChangeEvent ->

                companyNameArr[0] = valueChangeEvent.getValue()

        );



        nip.addValueChangeListener(valueChangeEvent ->
                NIPArr[0] = valueChangeEvent.getValue());

        address.addValueChangeListener(valueChangeEvent ->
                addressArr[0] = valueChangeEvent.getValue());

        phone.addValueChangeListener(valueChangeEvent ->
                phoneArr[0] = valueChangeEvent.getValue());
        }

    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MainGui.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
