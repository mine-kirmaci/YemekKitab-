package com.minekirmaci.yemekkitabi.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.minekirmaci.yemekkitabi.databinding.FragmentTarifBinding
import com.minekirmaci.yemekkitabi.model.Tarif
import com.minekirmaci.yemekkitabi.roomdb.TarifDAO
import com.minekirmaci.yemekkitabi.roomdb.TarifDataBase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream


class TarifFragment : Fragment() {
    private var _binding: FragmentTarifBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var secilenGorsel: Uri? = null
    private var secilenBitmap: Bitmap? = null
    private lateinit var db:TarifDataBase
    private lateinit var tarifDao:TarifDAO
    private val mDisposable= CompositeDisposable()
    private var secilenTarif:Tarif?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()

        db= Room.databaseBuilder(requireContext(),TarifDataBase::class.java,"Tarifler")
            .allowMainThreadQueries()
            .build()

        tarifDao=db.tarifDao()

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTarifBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageView.setOnClickListener { görselSec(it) }
        binding.Sil.setOnClickListener { sil(it) }
        binding.kaydet.setOnClickListener { kaydet(it) }

        arguments?.let {
            val bilgi = TarifFragmentArgs.fromBundle(it).bilgi
            if (bilgi == "yeni") {
                //yeni tarif eklenecek
                 secilenTarif=null
                binding.Sil.isEnabled = false
                binding.kaydet.isEnabled = true
                binding.isimText.setText("")//başlangıçta böyle gözüksün
                binding.malzemeText.setText("")
            } else {
                //eski eklenmiş tarif gösterilecek
                binding.Sil.isEnabled = true
                binding.kaydet.isEnabled = false
                val id=TarifFragmentArgs.fromBundle(it).id

                mDisposable.add(
                    tarifDao.findById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponse)
                )

            }
        }
    }

    private fun handleResponse(tarif:Tarif){
        val bitmap=BitmapFactory.decodeByteArray(tarif.gorsel,0,tarif.gorsel.size)
        binding.imageView.setImageBitmap(bitmap)
        binding.isimText.setText(tarif.isim)
        binding.malzemeText.setText(tarif.malzeme)
        secilenTarif=tarif
    }

    fun kaydet(view: View) {
        val isim = binding.isimText.text.toString()
        val malzeme = binding.malzemeText.text.toString()
        if(secilenBitmap!=null){
            val kucukBitmap=kucukBitMapOlustur(secilenBitmap!!,300)
            val outputStream=ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi=outputStream.toByteArray()

            val tarif= Tarif(isim,malzeme,byteDizisi)

            //RxJava
            mDisposable.add(
                tarifDao.insert(tarif)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponseForInsert)
            )
             }
    }

    private fun handleResponseForInsert(){
     //bir önceki fragmenta dön
        val action=TarifFragmentDirections.actionTarifFragmentToListeFragment()
        Navigation.findNavController(requireView()).navigate(action)

    }

    fun sil(view: View) {

        if(secilenTarif!=null){
            mDisposable.add(
                tarifDao.delete(tarif = secilenTarif!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsert)
            )
        }



    }

    fun görselSec(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //izin verilmemiş, izin istememiz gerek
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                ) {
                    //snackbar göstermemiz lazım, kullanıcıdan neden izin istediğimizi bir kez daha söyleyerek izin isteriz
                    Snackbar.make(
                        view,
                        "Galeriye ulaşıp görsel seçmemiz gerek",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(
                        "İzin ver",
                        View.OnClickListener {
                            //izin isteyeceğiz
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                    ).show()
                } else {
                    //izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else { //izin verilmiş galeriye gidilebilir
                val intentToGaleri =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGaleri)
            }


        } else {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //izin verilmemiş, izin istememiz gerek
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    //snackbar göstermemiz lazım, kullanıcıdan neden izin istediğimizi bir kez daha söyleyerek izin isteriz
                    Snackbar.make(
                        view,
                        "Galeriye ulaşıp görsel seçmemiz gerek",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(
                        "İzin ver",
                        View.OnClickListener {
                            //izin isteyeceğiz
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    ).show()
                } else {
                    //izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else { //izin verilmiş galeriye gidilebilir
                val intentToGaleri =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGaleri)
            }

        }

    }

    private fun registerLauncher() {

        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult != null) {
                        secilenGorsel = intentFromResult.data
                        try {
                            if (Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(
                                    requireActivity().contentResolver,
                                    secilenGorsel!!
                                )
                                secilenBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(secilenBitmap)
                            } else {
                                secilenBitmap = MediaStore.Images.Media.getBitmap(
                                    requireActivity().contentResolver,
                                    secilenGorsel
                                )
                                binding.imageView.setImageBitmap(secilenBitmap)
                            }
                        } catch (e: Exception) {
                            println(e.localizedMessage)
                        }
                    }

                }
            }

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    //izin verildi
                    //galeriye gidebiliriz
                    val intentToGaleri =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGaleri)
                } else {
                    //izin verilmedi
                    Toast.makeText(requireContext(), "İzin verilmedi", Toast.LENGTH_LONG).show()
                }

            }
    }

    private fun kucukBitMapOlustur(kullanicininSectigiBitMap: Bitmap, maximumBoyut: Int): Bitmap {
        var width = kullanicininSectigiBitMap.width
        var height = kullanicininSectigiBitMap.height
        var bitmapOrani: Double = width.toDouble() / height.toDouble()
        if (bitmapOrani > 1) {
            width = maximumBoyut
            val kisaltilmisYukseklik = width / bitmapOrani
            height = kisaltilmisYukseklik.toInt()
        } else {
            height = maximumBoyut
            val kisaltilmisGenislik = height * bitmapOrani
            width = kisaltilmisGenislik.toInt()
        }
        return Bitmap.createScaledBitmap(kullanicininSectigiBitMap,width,height,true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}