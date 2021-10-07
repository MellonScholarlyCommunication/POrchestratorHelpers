# Clean the archivator experiment
#  - https://hochstenbach.inrupt.net/archivator/

# Create the data directories if the not already exists
put /archivator/
put /archivator/inbox/
put /archivator/outbox/
put /archivator/outbox/
put /archivator/robustlinks/
put /archivator/orchestrator/
put /archivator/profile/
put /archivator/rules/

# Clean the data directories
emptyFolder /archivator/inbox/
emptyFolder /archivator/outbox/
emptyFolder /archivator/robustlinks/
emptyFolder /archivator/orchestrator/
