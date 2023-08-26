package ru.fivegst.speedtest.task.impl.model

import ru.fivegst.speedtest.client.service.api.ServiceApi
import ru.fivegst.speedtest.task.impl.BalancerApi

data class ApiClientHolder(
    val balancerApiClient: BalancerApi,
    val serviceApiClient: ServiceApi,
    val iperfAddress: ServerAddress,
)
