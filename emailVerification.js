const aws = require("aws-sdk");
const ses = new aws.SES({ region: "us-east-1" });
exports.handler = async function (event) {
    const message = JSON.parse(event.Records[0].Sns.Message);
    console.log("Message received from SNS:", message);
    const { username, one_time_token } = message;
    const params = {
        Destination: {
            ToAddresses: [username],
        },
        Message: {
            Body: {
                Text: {
                    Data:
                        `<h3>Hi ${username}!</h3>
                     <br/>
                     <p>Click the link below to verify your email address:
                     <a href="http://prod.rubyxjr.me:8080/v2/verifyUserEmail?email=${username}&token=${one_time_token}" "target="_blank">http://prod.rubyxjr.me:8080/v2/verifyUserEmail?email=${username}&token=${one_time_token}</a>
                     <p>
                     <br/>
                     Thank You!`
                },
            },
            Subject: { Data: "Email Verification" },
        },
        Source: "no-reply@prod.rubyxjr.me",
    };
    return ses.sendEmail(params).promise();
};