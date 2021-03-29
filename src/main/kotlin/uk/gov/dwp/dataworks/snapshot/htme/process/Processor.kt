package uk.gov.dwp.dataworks.snapshot.htme.process

interface Processor<I, O> {
    fun process(item: I): O?
}
