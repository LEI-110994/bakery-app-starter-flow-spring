package com.vaadin.starter.bakery.testbench;

import org.junit.jupiter.api.Assertions;

import com.vaadin.starter.bakery.testbench.elements.components.DashboardLCounterLabelElement;
import com.vaadin.starter.bakery.testbench.elements.ui.DashboardViewElement;
import com.vaadin.starter.bakery.testbench.elements.ui.StorefrontViewElement;
import com.vaadin.testbench.BrowserTest;

public class DashboardViewIT extends AbstractIT<DashboardViewElement> {

	@Override
	protected DashboardViewElement openView() {
		StorefrontViewElement storefront = openLoginView().login("admin@vaadin.com", "admin");
		return storefront.getMenu().navigateToDashboard();
	}

	@BrowserTest
	public void checkRowsCount() {
		DashboardViewElement dashboardPage = openView();
		Assertions.assertEquals(4, dashboardPage.getBoard().getRows().size());
	}

	@BrowserTest
	public void checkCounters() {
		DashboardViewElement dashboardPage = openView();
		int numLabels = dashboardPage.getBoard().getRows().get(0).$(DashboardLCounterLabelElement.class).all().size();
		Assertions.assertEquals(4, numLabels);
	}

}
