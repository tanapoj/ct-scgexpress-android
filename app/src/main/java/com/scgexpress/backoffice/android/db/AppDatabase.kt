package com.scgexpress.backoffice.android.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.scgexpress.backoffice.android.db.dao.*
import com.scgexpress.backoffice.android.db.entity.DeliveryEntity
import com.scgexpress.backoffice.android.db.entity.NotificationEntity
import com.scgexpress.backoffice.android.db.entity.TopicEntity
import com.scgexpress.backoffice.android.db.entity.TrackingPositionEntity
import com.scgexpress.backoffice.android.db.entity.masterdata.*


@Database(
    entities = [
        DeliveryEntity::class,
        NotificationEntity::class,
        TrackingPositionEntity::class,
        TblAuthUsers::class,
        TblManifestItems::class,
        TblManifestOfdItems::class,
        TblManifestOfdSheets::class,
        TblManifestSheets::class,
        TblManifestSheetsGeneral::class,
        TblMasterCustomerType::class,
        TblMasterDataSource::class,
        TblMasterIrregularType::class,
        TblMasterManifestType::class,
        TblMasterOperationCode::class,
        TblMasterParcelProperties::class,
        TblMasterParcelSizing::class,
        TblMasterParcelStatus::class,
        TblMasterPaymentType::class,
        TblMasterPromocode::class,
        TblMasterRetentionReason::class,
        TblMasterReturnReason::class,
        TblMasterServiceTypeLevel1::class,
        TblMasterServiceTypeLevel2::class,
        TblMasterServiceTypeLevel3::class,
        TblParcelStatus::class,
        TblPrice::class,
        TblReturnreruteSheets::class,
        TblScgAssortCode::class,
        TblScgBranch::class,
        TblScgCodModel::class,
        TblScgCodTier::class,
        TblScgConsignment::class,
        TblScgConsignmentDel::class,
        TblScgNekoBilling::class,
        TblScgNekoTracking::class,
        TblScgParcelReturnrerute::class,
        TblScgParcelTracking::class,
        TblScgParcels::class,
        TblScgPostalcode::class,
        TblScgPriceTier::class,
        TblScgPricingModel::class,
        TblScgReceipt::class,
        TblScgRegion::class,
        TblScgRegionDiff::class,
        TblMasterBillingCompany::class,
        TblOrganization::class,
        TblScgApiToken::class,
        TblScgUnregisteredTracking::class,
        TblTempCal::class,
        TopicEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(EnumConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun deliveryDao(): DeliveryDao
    abstract fun notificationDao(): NotificationDao
    abstract fun masterDataDao(): MasterDataDao
    abstract fun tblMasterParcelSizingDao(): TblMasterParcelSizingDao
    abstract fun tblMasterServiceTypeLevel3Dao(): TblMasterServiceTypeLevel3Dao
    abstract fun tblOrganizationDao(): TblOrganizationDao
    abstract fun trackingPositionDao(): TrackingPositionDao
    abstract fun topicDao(): TopicDao
}