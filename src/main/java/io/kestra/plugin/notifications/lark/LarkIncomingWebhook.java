package io.kestra.plugin.notifications.lark;

import io.kestra.core.http.HttpRequest;
import io.kestra.core.http.HttpResponse;
import io.kestra.core.http.client.HttpClient;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.VoidOutput;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.JacksonMapper;
import io.kestra.plugin.notifications.AbstractHttpOptionsTask;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.net.URI;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Send a Lark(Feishu) message using an Incoming Webhook.",
    description =
        """
            A custom bot is a bot that can only be used in the current group chat. This type of robot can complete the message push by calling the webhook address in the current group chat without being reviewed by the tenant administrator.\
             When sending a POST request to a custom robot webhook address, the supported message formats include text, rich text, picture message and group business card, etc.\
             For Lark check the <a href="https://open.larksuite.com/document/client-docs/bot-v3/add-custom-bot">Custom bot usage guide</a> for more details.\
             For Feishu check the <a href="https://open.feishu.cn/document/client-docs/bot-v3/add-custom-bot">Custom bot usage guide</a> for more details.
            """)
@Plugin(
    examples = {
        @Example(
            title = "Send a Lark(Feishu) notification on a failed flow execution.",
            full = true,
            code =
                """
                    id: unreliable_flow
                    namespace: company.team

                    tasks:
                      - id: fail
                        type: io.kestra.plugin.scripts.shell.Commands
                        runner: PROCESS
                        commands:
                          - exit 1

                    errors:
                      - id: alert_on_failure
                        type: io.kestra.plugin.notifications.lark.LarkIncomingWebhook
                        url: "{{ secret('LARK_WEBHOOK') }}" # https://open.larksuite.com/open-apis/bot/v2/hook/xxxxxxxxxxxxxxxxxx or https://open.feishu.cn/open-apis/bot/v2/hook/xxxxxxxxxxxxxxxxxx
                        payload: |
                          {
                            "msg_type":"text",
                            "content": {
                               "text": "Failure alert for flow {{ flow.namespace }}.{{ flow.id }} with ID {{ execution.id }}"
                            }
                          }
                    """),
        @Example(
            title = "Send a Lark(Feishu) text message via incoming webhook with payload argument.",
            full = true,
            code =
                """
                    id: lark_incoming_webhook
                    namespace: company.team

                    tasks:
                      - id: send_lark_message
                        type: io.kestra.plugin.notifications.lark.LarkIncomingWebhook
                        url: "{{ secret('LARK_WEBHOOK') }}"
                        payload: |
                          {
                            "msg_type":"text",
                            "content": {
                               "text": "Failure alert for flow {{ flow.namespace }}.{{ flow.id }} with ID {{ execution.id }}"
                            }
                          }
                    """),
        @Example(
            title = "Send a Lark(Feishu) rich text message via incoming webhook with payload argument.",
            full = true,
            code =
                """
                    id: lark_incoming_webhook
                    namespace: company.team

                    tasks:
                      - id: send_lark_message
                        type: io.kestra.plugin.notifications.lark.LarkIncomingWebhook
                        url: "{{ secret('LARK_WEBHOOK') }}"
                        payload: |
                            {
                                "msg_type": "post",
                                "content": {
                                    "post": {
                                        "zh_cn": {
                                            "title": "Project Update Notification",
                                            "content": [
                                                [
                                                    {
                                                        "tag": "text",
                                                        "text": "Item has been updated: {{ flow.namespace }}.{{ flow.id }} with ID {{ execution.id }}"
                                                    },
                                                    {
                                                        "tag": "a",
                                                        "text": "Please check",
                                                        "href": "http://www.example.com/"
                                                    },
                                                    {
                                                        "tag": "at",
                                                        "user_id": "ou_18eac8********17ad4f02e8bbbb"
                                                    }
                                                ]
                                            ]
                                        }
                                    }
                                }
                            }
                    """),
        @Example(
            title = "Send a Lark(Feishu) group business card message via incoming webhook with payload argument.",
            full = true,
            code =
                """
                    id: lark_incoming_webhook
                    namespace: company.team

                    tasks:
                      - id: send_lark_message
                        type: io.kestra.plugin.notifications.lark.LarkIncomingWebhook
                        url: "{{ secret('LARK_WEBHOOK') }}"
                        payload: |
                          {
                            "msg_type": "share_chat",
                            "content": {
                              "share_chat_id": "oc_f5b1a7eb27ae2****339ff"
                            }
                          }
                    """),
        @Example(
            title = "Send a Lark(Feishu) pictures message via incoming webhook with payload argument.",
            full = true,
            code =
                """
                    id: lark_incoming_webhook
                    namespace: company.team

                    tasks:
                      - id: send_lark_message
                        type: io.kestra.plugin.notifications.lark.LarkIncomingWebhook
                        url: "{{ secret('LARK_WEBHOOK') }}"
                        payload: |
                          {
                            "msg_type": "image",
                            "content": {
                              "image_key": "img_ecffc3b9-8f14-400f-a014-05eca1a4310g"
                            }
                          }
                    """),
        @Example(
            title = "Send a Lark(Feishu) card message via incoming webhook with payload argument.",
            full = true,
            code =
                """
                    id: lark_incoming_webhook
                    namespace: company.team

                    tasks:
                      - id: send_lark_message
                        type: io.kestra.plugin.notifications.lark.LarkIncomingWebhook
                        url: "{{ secret('LARK_WEBHOOK') }}"
                        payload: |
                          {
                              "msg_type": "interactive",
                              "card": {
                                  "elements": [
                                      {
                                          "tag": "div",
                                          "text": {
                                              "content": "**West Lake**, located at No. 1 Longjing Road, Xihu District, Hangzhou City, Zhejiang Province, west of Hangzhou City, with a total area of 49 square kilometers, a catchment area of 21.22 square kilometers, and a lake area of 6.38 square kilometers km.",
                                              "tag": "lark_md"
                                          }
                                      },
                                      {
                                          "actions": [
                                              {
                                                  "tag": "button",
                                                  "text": {
                                                      "content": "More attractions introduction: Rose:",
                                                      "tag": "lark_md"
                                                  },
                                                  "url": "https://www.example.com",
                                                  "type": "default",
                                                  "value": {

                                                  }
                                              }
                                          ],
                                          "tag": "action"
                                      }
                                  ],
                                  "header": {
                                      "title": {
                                          "content": "Today's travel recommendation",
                                          "tag": "plain_text"
                                      }
                                  }
                              }
                          }
                    """),
    })
public class LarkIncomingWebhook extends AbstractHttpOptionsTask {
    @Schema(
        title = "Lark(Feishu) incoming webhook URL")
    @PluginProperty(dynamic = true)
    @NotEmpty
    private String url;

    @Schema(title = "Lark(Feishu) message payload")
    protected Property<String> payload;

    @Override
    public VoidOutput run(RunContext runContext) throws Exception {
        String renderedUrl = runContext.render(this.url);
        Object payloadObject = prepareMessage(runContext);

        runContext.logger().debug("Send Lark webhook: {}", payloadObject);
        try (HttpClient client =
                 new HttpClient(runContext, super.httpClientConfigurationWithOptions())) {
            HttpRequest.HttpRequestBuilder requestBuilder =
                createRequestBuilder(runContext)
                    .addHeader("Content-Type", "application/json")
                    .uri(URI.create(renderedUrl))
                    .method("POST")
                    .body(HttpRequest.JsonRequestBody.builder().content(payloadObject).build());

            HttpRequest request = requestBuilder.build();

            HttpResponse<String> response = client.request(request, String.class);

            runContext.logger().debug("Response: {}", response.getBody());

            if (response.getStatus().getCode() == 200) {
                runContext.logger().info("Request succeeded");
            }
        }
        return null;
    }

    private Object prepareMessage(RunContext runContext) throws Exception {
        if (payload != null) {
            String renderedPayload = runContext.render(payload).as(String.class).orElse(null);
            return JacksonMapper.ofJson().readTree(renderedPayload);
        }

        throw new IllegalArgumentException("'payload' must be provided");
    }
}
