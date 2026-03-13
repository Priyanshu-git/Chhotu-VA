package com.nexxlabs.chhotu.domain.repository

import com.nexxlabs.chhotu.domain.model.Contact

interface ContactRepository {
    fun findContactsByName(name: String): List<Contact>
}
