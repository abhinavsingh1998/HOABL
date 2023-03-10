package com.emproto.hoabl.di

import android.window.SplashScreen
import com.emproto.hoabl.HoablSplashActivity
import com.emproto.hoabl.feature.chat.views.fragments.ChatsDetailFragment
import com.emproto.hoabl.feature.chat.views.fragments.ChatsFragment
import com.emproto.hoabl.feature.home.views.HomeActivity
import com.emproto.hoabl.feature.home.views.fragments.*
import com.emproto.hoabl.feature.investment.views.*
import com.emproto.hoabl.feature.investment.views.mediagallery.*
import com.emproto.hoabl.feature.login.AuthActivity
import com.emproto.hoabl.feature.login.LoginFragment
import com.emproto.hoabl.feature.login.NameInputFragment
import com.emproto.hoabl.feature.login.OTPVerificationFragment
import com.emproto.hoabl.feature.portfolio.views.*
import com.emproto.hoabl.feature.profile.fragments.about_us.AboutUsFragment
import com.emproto.hoabl.feature.profile.fragments.accounts.AccountDetailsFragment
import com.emproto.hoabl.feature.profile.fragments.accounts.AllPaymentHistoryFragment
import com.emproto.hoabl.feature.profile.fragments.edit_profile.EditProfileFragment
import com.emproto.hoabl.feature.profile.fragments.faq.ProfileFaqFragment
import com.emproto.hoabl.feature.profile.fragments.feedback.FeedbackFragment
import com.emproto.hoabl.feature.profile.fragments.help_center.HelpCenterFragment
import com.emproto.hoabl.feature.profile.fragments.privacy.PrivacyFragment
import com.emproto.hoabl.feature.profile.fragments.profile.ProfileFragment
import com.emproto.hoabl.feature.profile.fragments.securtiyandsettings.SecurityFragment
import com.emproto.hoabl.feature.profile.fragments.securtiyandsettings.SecurityTipsFragment
import com.emproto.hoabl.feature.promises.HoablPromises
import com.emproto.hoabl.feature.promises.PromisesDetailsFragment
import com.emproto.hoabl.notification.MyFirebasemessagingService
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [HomeAppModule::class])
interface HomeComponent {
    fun inject(activity: AuthActivity)
    fun inject(activity: HomeActivity)
    fun inject(homeFragment: HomeFragment)
    fun inject(loginFragment: LoginFragment)
    fun inject(fragment: OTPVerificationFragment)
    fun inject(fragment: NameInputFragment)
    fun inject(activity: HoablSplashActivity)
    fun inject(fragment: ProfileFragment)
    fun inject(fragment: HoablPromises)
    fun inject(fragment: PromisesDetailsFragment)
    fun inject(fragment: InvestmentFragment)
    fun inject(fragment: ProjectDetailFragment)
    fun inject(fragment: PortfolioFragment)
    fun inject(service: MyFirebasemessagingService)
    fun inject(fragment: PortfolioSpecificProjectView)
    fun inject(fragment: EditProfileFragment)
    fun inject(fragment: ProjectTimelineFragment)
    fun inject(fragment: FaqDetailFragment)
    fun inject(fragment: LandSkusFragment)
    fun inject(fragment: Testimonials)
    fun inject(fragment: InsightsDetailsFragment)
    fun inject(fragment: LatestUpdatesDetailsFragment)
    fun inject(fragment: LatestUpdatesFragment)
    fun inject(fragment: InsightsFragment)
    fun inject(fragment: SplashScreen)
    fun inject(fragment: CategoryListFragment)
    fun inject(fragment: MapFragment)
    fun inject(fragment: OpportunityDocsFragment)
    fun inject(fragment: MediaGalleryFragment)
    fun inject(fragment: PhotosFragment)
    fun inject(fragment: MediaViewFragment)
    fun inject(fragment: VideosFragment)
    fun inject(fragment: ReferralDialog)
    fun inject(fragment: DroneFragment)
    fun inject(fragment: ThreeSixtyFragment)
    fun inject(fragment: DocViewerFragment)
    fun inject(fragment: ChatsFragment)
    fun inject(fragment: ChatsDetailFragment)
    fun inject(fragment: BookingJourneyFragment)
    fun inject(fragment: FeedbackFragment)
    fun inject(fragment: PrivacyFragment)
    fun inject(fragment: SearchResultFragment)
    fun inject(fragment: AccountDetailsFragment)
    fun inject(fragment: AllPaymentHistoryFragment)
    fun inject(fragment: AboutUsFragment)
    fun inject(fragment: ProfileFaqFragment)
    fun inject(fragment: ReceiptFragment)
    fun inject(fragment: SecurityFragment)
    fun inject(fragment: SecurityTipsFragment)
    fun inject(fragment: FmFragment)
    fun inject(fragment: HelpCenterFragment)

}