import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.MoveEvent;
import org.primefaces.util.Constants;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.FacesEvent;
import javax.faces.event.PhaseId;
import javax.el.ELContext;
import javax.faces.event.BehaviorEvent;

    public static final String CONTAINER_CLASS = "ui-dialog ui-widget ui-widget-content ui-corner-all ui-shadow ui-hidden-container";
    public static final String TITLE_BAR_CLASS = "ui-dialog-titlebar ui-widget-header ui-helper-clearfix ui-corner-top";
    public static final String TITLE_CLASS = "ui-dialog-title";
    public static final String TITLE_BAR_CLOSE_CLASS = "ui-dialog-titlebar-icon ui-dialog-titlebar-close ui-corner-all";
    public static final String CLOSE_ICON_CLASS = "ui-icon ui-icon-closethick";
    public static final String TITLE_BAR_MINIMIZE_CLASS = "ui-dialog-titlebar-icon ui-dialog-titlebar-minimize ui-corner-all";
    public static final String MINIMIZE_ICON_CLASS = "ui-icon ui-icon-minus";
    public static final String TITLE_BAR_MAXIMIZE_CLASS = "ui-dialog-titlebar-icon ui-dialog-titlebar-maximize ui-corner-all";
    public static final String MAXIMIZE_ICON_CLASS = "ui-icon ui-icon-extlink";
    public static final String CONTENT_CLASS = "ui-dialog-content ui-widget-content";
    public static final String FOOTER_CLASS = "ui-dialog-footer ui-widget-content";

    public static final String ARIA_CLOSE = "primefaces.dialog.aria.CLOSE";

    private final static String DEFAULT_EVENT = "close";

    private static final Map<String, Class<? extends BehaviorEvent>> BEHAVIOR_EVENT_MAPPING = Collections.unmodifiableMap(new HashMap<String, Class<? extends BehaviorEvent>>() {{
        put("close", CloseEvent.class);
        put("minimize", null);
        put("maximize", null);
        put("move", MoveEvent.class);
        put("restoreMinimize", null);
        put("restoreMaximize", null);
        put("open", null);
        put("loadContent", null);
    }});

    private static final Collection<String> EVENT_NAMES = BEHAVIOR_EVENT_MAPPING.keySet();

    @Override
    public Map<String, Class<? extends BehaviorEvent>> getBehaviorEventMapping() {
         return BEHAVIOR_EVENT_MAPPING;
    }

    @Override
    public Collection<String> getEventNames() {
        return EVENT_NAMES;
    }

    @Override
    public String getDefaultEventName() {
        return DEFAULT_EVENT;
    }

    @Override
    public void queueEvent(FacesEvent event) {
        FacesContext context = getFacesContext();

        if(ComponentUtils.isRequestSource(this, context) && event instanceof AjaxBehaviorEvent) {
            Map<String,String> params = context.getExternalContext().getRequestParameterMap();
            String eventName = params.get(Constants.RequestParams.PARTIAL_BEHAVIOR_EVENT_PARAM);
            AjaxBehaviorEvent ajaxBehaviorEvent = (AjaxBehaviorEvent) event;
            String clientId = getClientId(context);

            if(eventName.equals("close")) {
                setVisible(false);
                CloseEvent closeEvent = new CloseEvent(this, ((AjaxBehaviorEvent) event).getBehavior());
                closeEvent.setPhaseId(ajaxBehaviorEvent.getPhaseId());
                super.queueEvent(closeEvent);
            }
            else if(eventName.equals("move")) {
                int top = Double.valueOf(params.get(clientId + "_top")).intValue();
                int left = Double.valueOf(params.get(clientId + "_left")).intValue();
                MoveEvent moveEvent = new MoveEvent(this, ((AjaxBehaviorEvent) event).getBehavior(), top, left);
                moveEvent.setPhaseId(ajaxBehaviorEvent.getPhaseId());
                super.queueEvent(moveEvent);
            }
            else {
                //minimize and maximize
                super.queueEvent(event);
            }
        } else {
            super.queueEvent(event);
        }
    }

    @Override
    public void processDecodes(FacesContext context) {
        if(ComponentUtils.isRequestSource(this, context)) {
            this.decode(context);
        }
        else {
            super.processDecodes(context);
        }
    }

    @Override
    public void processValidators(FacesContext context) {
        if(!ComponentUtils.isRequestSource(this, context)) {
            super.processValidators(context);
        }
    }

    @Override
    public void processUpdates(FacesContext context) {
        if(!ComponentUtils.isRequestSource(this, context)) {
            super.processUpdates(context);
        }
        else {
            ValueExpression visibleVE = this.getValueExpression(PropertyKeys.visible.toString());
            if(visibleVE != null) {
                FacesContext facesContext = getFacesContext();
                ELContext eLContext = facesContext.getELContext();

                if(!visibleVE.isReadOnly(eLContext)) {
                    visibleVE.setValue(eLContext, this.isVisible());
                    this.getStateHelper().put(PropertyKeys.visible, null);
                }
            }
        }
    }


    public boolean isContentLoadRequest(FacesContext context) {
        return context.getExternalContext().getRequestParameterMap().containsKey(this.getClientId(context) + "_contentLoad");
    }

    public boolean isRTL() {
        return this.getDir().equalsIgnoreCase("rtl");
    }
