package com.scgexpress.backoffice.android.db.entity.masterdata

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

@Entity(tableName = "tbl_master_service_type_level_3")
data class TblMasterServiceTypeLevel3(
    @PrimaryKey @Json(name = "id") @SerializedName("id") @ColumnInfo(name = "id") var id: Int = 0,
    @Json(name = "id_service_type_level_2") @SerializedName("id_service_type_level_2") @ColumnInfo(name = "id_service_type_level_2") var idServiceTypeLevel2: Int = 0,
    @Json(name = "status_deleted") @SerializedName("status_deleted") @ColumnInfo(name = "status_deleted") var statusDeleted: String? = "N",
    @Json(name = "status_active") @SerializedName("status_active") @ColumnInfo(name = "status_active") var statusActive: String? = "Y",
    @Json(name = "code_service_type_level_3") @SerializedName("code_service_type_level_3") @ColumnInfo(name = "code_service_type_level_3") var codeServiceTypeLevel3: String? = "",
    @Json(name = "name") @SerializedName("name") @ColumnInfo(name = "name") var name: String? = "",
    @Json(name = "title") @SerializedName("title") @ColumnInfo(name = "title") var title: String? = "",
    @Json(name = "description") @SerializedName("description") @ColumnInfo(name = "description") var description: String? = "",
    @Json(name = "date_created") @SerializedName("date_created") @ColumnInfo(name = "date_created") var dateCreated: String? = "",
    @Json(name = "date_modified") @SerializedName("date_modified") @ColumnInfo(name = "date_modified") var dateModified: String? = ""
) {
    @Ignore
    constructor() : this(0)
}