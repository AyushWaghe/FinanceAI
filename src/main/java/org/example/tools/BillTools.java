package org.example.tools;

import lombok.RequiredArgsConstructor;
import org.example.client.BillClient;
import org.example.dto.APIResponse;
import org.example.dto.BillInstanceResponse;
import org.example.dto.BillResponse;
import org.example.enums.BillStatus;
import org.example.util.AuthenticationUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BillTools {

    private final BillClient billClient;

    @Tool(
            name = "get_bills",
            description = """
        Get users all LATEST/LIVE recurrent bills belonging to the current user.

        Use this tool when the user asks about their recurrent bills,
        The recurrence is one from DAILY,WEEKLY,MONTHLY,QUARTERLY,HALF_YEARLY, YEARLY
        """
    )
    public List<BillResponse> getBills() {
        System.out.println("Calling get_bills");
        Integer userId = getAuthenticatedUserId();
        return billClient.getBills(userId);
    }

    @Tool(
            name = "get_upcoming_bills",
            description = """
        Get LATEST/LIVE all pending bill instances for the current user
        starting from the current date.

        Use this tool when the user asks about:
        - upcoming bills
        - pending bills
        - bills that need to be paid
        - what bills are due
        """
    )
    public List<BillInstanceResponse> getUpcomingBills() {
        System.out.println("Calling getUpcomingBills");
        Integer userId = getAuthenticatedUserId();
        return billClient.getUpcomingBills(userId);
    }

    @Tool(
            name = "get_bills_by_status",
            description = """
        Get the user's LATEST/LIVE bill instances filtered by payment status.

        status must be one of:
        - PENDING
        - PAID
        - OVERDUE

        Use this tool when the user asks for bills based
        on their payment status.
        """
    )
    public List<BillInstanceResponse> getBillsByStatus(
            @ToolParam(
                    description = "Bill status is either PENDING,OVERDUE or PAID"
            )
            BillStatus status) {
        System.out.println("Calling getBillsByStatus");
        Integer userId = getAuthenticatedUserId();
        return billClient.getBillsByStatus(userId, status);
    }

    @Tool(
            name = "get_overdue_bills",
            description = """
        Get the user's LATEST/LIVE record of overdue bills.

        An overdue bill is a bill whose payment status is PENDING
        and whose due date has already passed. That is due date is lesser than the current date.

        Use this tool when the user asks about overdue,
        late, or missed bill payments.
        """
    )
    public List<BillInstanceResponse> getOverdueBills() {
        System.out.println("Calling getOverdueBills");
        Integer userId = getAuthenticatedUserId();
        return billClient.getOverdueBills(userId);
    }


    private Integer getAuthenticatedUserId() {
        return AuthenticationUtil.getCurrentUserId();
    }
}
