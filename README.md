Map reduce version of HTME exported calculation parts in 6 hours, current version of HTME takes 8 hours. If we are to pursue further gains the options would be to look into
tuning the map reduce configuration of the cluster
convert the map reduce solution to a spark job as this will allow decryption to occur in something more akin to the map phase when there is more parallelism
We could also choose to use the map reduce version as is either on all collections (though it is not any quicker on smaller collections) or just the largest, or we could continue with HTME.
