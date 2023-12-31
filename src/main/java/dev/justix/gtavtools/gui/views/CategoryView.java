package dev.justix.gtavtools.gui.views;

import dev.justix.gtavtools.gui.components.View;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CategoryView extends View {

    private final List<Tool> components;
    private final Category category;

    public CategoryView(MainView mainView, Category category) {
        super(mainView, category.getDisplayName());

        this.components = new ArrayList<>();
        this.category = category;
    }

    public void addTool(Tool tool) {
        this.components.add(tool);
    }

}
