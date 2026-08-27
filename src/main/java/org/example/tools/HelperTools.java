package org.example.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class HelperTools {
    @Tool(
            name = "get_todays_date_and_time",
            description = """
       Use this tool to know what is today's date and time. This function will return day,month and year and time at the time user is asking the query. Use this tool to answer user query relative to current day,month or year.
        """
    )
    public LocalDateTime getCurrentDateAndTime() {
        System.out.println("Calling date_function");
        return LocalDateTime.now();
    }
}
