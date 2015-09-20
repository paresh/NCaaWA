package unikl.disco.dnc.client;



import java.awt.event.MouseAdapter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import com.allen_sauer.gwt.dnd.client.HasDragHandle;

/**
 * Widget wrapper class used by {@link PalettePanel}.
 */
public class PaletteWidget extends AbsolutePanel implements HasDragHandle {

  private FocusPanel shim = new FocusPanel();

  private final Widget widget;

  /**
   * Default constructor to wrap the provided widget.
   * 
   * @param widget the widget to be wrapped
   */
  public PaletteWidget(Widget widget) {
    this.widget = widget;
    add(widget);

    // Add some CSS styling
    /*addStyleName("demo-PaletteWidget");
    widget.addStyleName("demo-PaletteWidget-widget");
    shim.addStyleName("demo-PaletteWidget-shim");*/
  }

  public PaletteWidget(Image img) {
	    this.widget = img;
	    add(widget);

	    
	  }

  
  public PaletteWidget cloneWidget() {
    Widget clone;

    // Clone our internal widget
    if (widget instanceof Label) {
      Label label = (Label) widget;
      clone = new Label(label.getText());
    } else if (widget instanceof Image) {
      Image image=(Image)widget;
      clone=new Image("images/server.png");
    } else if (widget instanceof CheckBox) {
      CheckBox checkBox = (CheckBox) widget;
      clone = new CheckBox(checkBox.getHTML(), true);
    }else if (widget instanceof Button) {
        Button button = (Button) widget;
        clone = new Button("Button");
        
    } 
    else {
      throw new IllegalStateException("Unhandled Widget class " + widget.getClass().getName());
    }

    // Copy a few obvious common widget properties
    clone.setStyleName(widget.getStyleName());
    clone.setTitle(widget.getTitle());

    // Wrap the cloned widget in a new PaletteWidget instance
    return new PaletteWidget(clone);
  }
  
 

  @Override
  public Widget getDragHandle() {
	  
	  
    return shim;
  }

  /**
   * Let shim size match our size.
   * 
   * @param width the desired pixel width
   * @param height the desired pixel height
   */
  @Override
  public void setPixelSize(int width, int height) {
    super.setPixelSize(width, height);
    shim.setPixelSize(width, height);
  }

  /**
   * Let shim size match our size.
   * 
   * @param width the desired CSS width
   * @param height the desired CSS height
   */
  @Override
  public void setSize(String width, String height) {
    super.setSize(width, height);
    shim.setSize(width, height);
  }

  /**
   * Adjust the shim size and attach once our widget dimensions are known.
   */
  @Override
  protected void onLoad() {
    super.onLoad();
    shim.setPixelSize(getOffsetWidth(), getOffsetHeight());
    add(shim, 0, 0);
  }

  /**
   * Remove the shim to allow the widget to size itself when reattached.
   */
  @Override
  protected void onUnload() {
    super.onUnload();
    shim.removeFromParent();
  }
}