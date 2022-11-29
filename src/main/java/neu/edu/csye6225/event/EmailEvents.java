package neu.edu.csye6225.event;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.BatchWriteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.simpleemail.model.*;

import com.google.gson.Gson;

public class EmailEvents implements RequestHandler<SNSEvent, Object> {

    @Override
    public Object handleRequest(SNSEvent snsEvent, Context context) {
        String record = snsEvent.getRecords().get(0).getSNS().getMessage();
        UserMessage m = new Gson().fromJson(record, UserMessage.class);

        String FROM = "no-reply@prod.rubyxjr.me";
        String TO = m.getUsername();
        String SUBJECT = "User Verification Email";
        String FIRST_NAME = m.getFirst_name();
        String LINK = m.getLink();

        String TEXTBODY = "Hi " + FIRST_NAME + "! Here is your verification link, please click it to complete verification: " + LINK;

        try {
            AmazonSimpleEmailService client =
                    AmazonSimpleEmailServiceClientBuilder.standard()
                            .withRegion(Regions.US_EAST_1).build();
            SendEmailRequest emailRequest = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(TO))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withText(new Content()
                                            .withCharset("UTF-8").withData(TEXTBODY)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(SUBJECT)))
                    .withSource(FROM);

            client.sendEmail(emailRequest);
            context.getLogger().log("Email sent!");
        } catch (Exception ex) {
            context.getLogger().log("The email was not sent. Error message: "
                    + ex.getMessage());
        }

        context.getLogger().log("Record Message:" + record);

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDB = new DynamoDB(client);
        String tableName = "StatusTable";

        try {

            TableWriteItems tableWriteItems = new TableWriteItems(tableName) // Forum
                    .withItemsToPut(new Item().withPrimaryKey("email", m.getUsername())
                            .withString("Status", "Sent"));
            BatchWriteItemOutcome outcome = dynamoDB.batchWriteItem(tableWriteItems);

            context.getLogger().log("item added to Status table");

        }
        catch (Exception e) {
            context.getLogger().log("Fail to add item. Error message: "
                    + e.getMessage());
        }

        return null;
    }

}
