package unikl.disco.dnc.client;



import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.allen_sauer.gwt.dnd.client.PickupDragController;


public class PalettePanel extends VerticalPanel {

  private final PickupDragController dragController;

  public PalettePanel(PickupDragController dragController) {
    this.dragController = dragController;
    //addStyleName("demo-PalettePanel");
    //setSpacing(2);
   // setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

    Label header = new Label("Widget Palette");
    header.addStyleName("demo-PalettePanel-header");
    add(header);
  }

  
  public void add(PaletteWidget w) {
    dragController.makeDraggable(w);
    super.add(w);
  }

  
  @Override
  public boolean remove(Widget w) {
    int index = getWidgetIndex(w);
    if (index != -1 && w instanceof PaletteWidget) {
      PaletteWidget clone = ((PaletteWidget) w).cloneWidget();
      dragController.makeDraggable(clone);
      insert(clone, index);
    }
    return super.remove(w);
  }
}