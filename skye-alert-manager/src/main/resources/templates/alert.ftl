【生产环境告警】
<#list alerts as alert>
${alert.summary}
<#if alert.grafanaLink??>
    ==> Grafana：${alert.grafanaLink}
</#if>
<#if alert.skyeLink??>
    ==> Skye：${alert.skyeLink}
</#if>
</#list>