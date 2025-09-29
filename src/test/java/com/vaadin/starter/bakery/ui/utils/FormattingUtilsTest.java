/**
 *
 */
package com.vaadin.starter.bakery.ui.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.vaadin.starter.bakery.test.FormattingTest;
import com.vaadin.starter.bakery.ui.utils.converters.LocalDateTimeConverter;

public class FormattingUtilsTest extends FormattingTest {

	@Test
	public void formatAsCurrencyShouldBeLocaleIndependent() {
		String result = FormattingUtils.formatAsCurrency(987654345);
		Assertions.assertEquals("$9,876,543.45", result);
	}

	@Test
	public void getUiPriceFormatterShouldBeLocaleIndependent() {
		String result = FormattingUtils.getUiPriceFormatter().format(9876543);
		Assertions.assertEquals("9876543.00", result);
	}

	@Test
	public void shortDayFormatterShouldBeLocaleIndependent() {
		String result = FormattingUtils.SHORT_DAY_FORMATTER.format(LocalDate.of(2017, 11, 13));
		Assertions.assertEquals("Mon 13", result);
	}

	@Test
	public void weekdayFullDayFormatterShouldBeLocaleIndependent() {
		String result = FormattingUtils.WEEKDAY_FULLNAME_FORMATTER.format(LocalDate.of(2017, 10, 13));
		Assertions.assertEquals("Friday", result);
	}

	@Test
	public void monthAndDayFormatterShouldBeLocaleIndependent() {
		String result = FormattingUtils.MONTH_AND_DAY_FORMATTER.format(LocalDate.of(2015, 6, 26));
		Assertions.assertEquals("Jun 26", result);
	}

	@Test
	public void weekNumberShouldBeLocaleIndependent() {
		Assertions.assertEquals(getWeek(2017, 9, 3), getWeek(2017, 9, 9));
		Assertions.assertNotEquals(getWeek(2017, 9, 2), getWeek(2017, 9, 4));
		Assertions.assertNotEquals(getWeek(2017, 9, 8), getWeek(2017, 9, 10));
	}

	@Test
	public void fullDateformatterShouldBeLocaleIndependent() {
		String result = FormattingUtils.FULL_DATE_FORMATTER.format(LocalDateTime.of(2016, 11, 27, 22, 15, 33));
		Assertions.assertEquals("27.11.2016", result);
	}

	@Test
	public void getFullMonthNameShouldBeLocaleIndependent() {
		String result = FormattingUtils.getFullMonthName(LocalDate.of(2003, 8, 22));
		Assertions.assertEquals("August", result);
	}

	@Test
	public void timeConverterShouldFormatDateWithAmPm() {
		LocalDateTimeConverter lt = new LocalDateTimeConverter();
		String result = lt.encode(LocalDateTime.of(2016, 11, 27, 22, 15, 33));
		Assertions.assertEquals("27.11.2016 10:15 PM", result);
	}

	private int getWeek(int year, int month, int day) {
		return LocalDate.of(year, month, day).get(FormattingUtils.WEEK_OF_YEAR_FIELD);
	}
}
