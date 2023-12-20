package dev.justix.gtavtools.gui.components;

import dev.justix.gtavtools.gui.views.CategoryView;
import dev.justix.gtavtools.tools.Tool;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class View extends Component {

    private int currentIndex;

    public View(View parent, String name) {
        super(parent, name);

        if (parent instanceof CategoryView categoryView)
            categoryView.addTool((Tool) this);

        this.currentIndex = 0;
    }

    public abstract List<? extends Component> getComponents();

}
