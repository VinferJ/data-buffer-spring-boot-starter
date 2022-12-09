# Buffer Oriented Data Consume Component
## Introduction
when we have some business need to query some original data from db, and those data needs to be split to many parts, each part's amount will less than or equals to the original data amount.<br>
but in some situation we still want to consume each split parts until their data amount accumulated to the same amount as original, so we need some places to accumulate these data, and we name them data buffer, when they are accumulated to the amount we want, notify the listener or consumer to consume them.<br>

## Design
this component architecture is based on producer-consumer model and refer the epoll model.<br>
