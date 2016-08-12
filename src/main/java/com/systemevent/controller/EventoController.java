package com.systemevent.controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.systemevent.entity.Evento;
import com.systemevent.jsfclass.util.JsfUtil;
import com.systemevent.jsfclass.util.JsfUtil.PersistAction;
import com.systemevent.dao.EventoFacade;
import com.systemevent.jsfclass.util.Util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

@Named("eventoController")
@SessionScoped
public class EventoController implements Serializable {

    @EJB
    private com.systemevent.dao.EventoFacade ejbFacade;
    private List<Evento> items = null;
    private Evento selected;
   
    public EventoController() {
    }

    public Evento getSelected() {
        return selected;
    }

    public void setSelected(Evento selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private EventoFacade getFacade() {
        return ejbFacade;
    }

    public Evento prepareCreate() {
        selected = new Evento();
        initializeEmbeddableKey();
        return selected;
    }
    
    public String consultarForm() {
       return "consulta_evento_form";
    }
    
    public String crearForm() {
       return "crear_evento_form";
    }
    
 

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("EventoCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/Bundle").getString("EventoUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/Bundle").getString("EventoDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Evento> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }

    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            setEmbeddableKeys();
            try {
                if (persistAction != PersistAction.DELETE) {
                    getFacade().edit(selected);
                } else {
                    getFacade().remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
            } catch (EJBException ex) {
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage(msg);
                } else {
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            }
        }
    }

    public Evento getEvento(java.lang.Integer id) {
        return getFacade().find(id);
    }

    public List<Evento> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Evento> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }
    
    public Evento buscarPDF(java.lang.Integer id){
            return getFacade().find(id);
    }
    
    
       public void imprimir(int a) {
           Evento event = buscarPDF(a);
         
           //buscarPDF(n);
        try {
            //Generamos el archivo PDF
            String directorioArchivos;
            ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
            directorioArchivos = ctx.getRealPath("\'") + "reports";
            String name = directorioArchivos + "\'document-report.pdf";
            //String name="C:\\Users\\Jose_Gascon\\Documents\\NetBeansProjects\\SystemEvent\\target\\SystemEvent-1.0-SNAPSHOT\\reports\\document-report.pdf";
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(name));
            document.open();
            
            document.add(new Paragraph("Visita http://rolandopalermo.blogspot.com"));
            document.add(new Paragraph("Nombre: " + event.getDescripcion()));
            document.add(new Paragraph("Tipo: " + event.getUbicacion()));
            document.add(new Paragraph("Precio neto: " +event.getCodigoPais().getNombre()));
            document.add(new Paragraph("Valor neto: " + "prueba"));
            document.add(new Paragraph("Síguenos en http://facebook.com/blog.rolandopalermo"));
            document.close();
            //----------------------------
            //Abrimos el archivo PDF
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
            response.setContentType("application/pdf");
            response.setHeader("Content-disposition",
                    "inline=filename=" + name);
            try {
                response.getOutputStream().write(Util.getBytesFromFile(new File(name)));  
                response.getOutputStream().flush();
                response.getOutputStream().close();
                context.responseComplete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FacesConverter(forClass = Evento.class)
    public static class EventoControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            EventoController controller = (EventoController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "eventoController");
            return controller.getEvento(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Evento) {
                Evento o = (Evento) object;
                return getStringKey(o.getCodigoEvento());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Evento.class.getName()});
                return null;
            }
        }

    }

}