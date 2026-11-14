package com.krakedev.taller_jwt.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.multipart.MultipartFile;

import com.krakedev.taller_jwt.entidades.Vehiculo;
import com.krakedev.taller_jwt.repositorios.VehiculoRepository;


@RestController
@RequestMapping("/auth/vehiculos")
@CrossOrigin(origins = "http://localhost:5173")
public class VehiculoController {

    private final VehiculoRepository vehiculoRepository;


    public VehiculoController(
            VehiculoRepository vehiculoRepository) {

        this.vehiculoRepository = vehiculoRepository;
    }


    /*
     * CREATE
     * POST /auth/vehiculos/registrar
     */
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarVehiculo(

            @RequestParam("file") MultipartFile file,

            @RequestParam("marca") String marca,

            @RequestParam("modelo") String modelo) {


        try {

            if (file.isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(
                            "Debe seleccionar una imagen"
                        );
            }


            Vehiculo vehiculo = new Vehiculo();

            vehiculo.setMarca(marca);

            vehiculo.setModelo(modelo);

            vehiculo.setMimeType(
                    file.getContentType()
            );

            vehiculo.setFoto(
                    file.getBytes()
            );


            vehiculoRepository.save(vehiculo);


            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                        "Vehiculo registrado con exito"
                    );


        } catch (Exception e) {

            return ResponseEntity
                    .status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                        "Error al registrar vehiculo: "
                        + e.getMessage()
                    );
        }
    }


    /*
     * READ
     * GET /auth/vehiculos
     */
    @GetMapping
    public ResponseEntity<List<Vehiculo>>
            listarVehiculos() {


        List<Vehiculo> lista =
                vehiculoRepository.findAll();


        /*
         * No enviamos los bytes pesados
         * dentro del JSON.
         *
         * La fotografía se obtiene mediante
         * /{id}/foto
         */
        for (Vehiculo vehiculo : lista) {

            vehiculo.setFoto(null);
        }


        return ResponseEntity.ok(lista);
    }


    /*
     * READ IMAGEN
     * GET /auth/vehiculos/{id}/foto
     */
    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> obtenerFoto(

            @PathVariable Integer id) {


        Optional<Vehiculo> optional =
                vehiculoRepository.findById(id);


        if (optional.isEmpty()) {

            return ResponseEntity.notFound().build();
        }


        Vehiculo vehiculo = optional.get();


        if (vehiculo.getFoto() == null
                || vehiculo.getMimeType() == null) {

            return ResponseEntity.notFound().build();
        }


        HttpHeaders headers =
                new HttpHeaders();


        headers.setContentType(
                MediaType.parseMediaType(
                        vehiculo.getMimeType()
                )
        );


        return new ResponseEntity<>(
                vehiculo.getFoto(),
                headers,
                HttpStatus.OK
        );
    }


    /*
     * UPDATE
     * PUT /auth/vehiculos/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarVehiculo(

            @PathVariable Integer id,

            @RequestParam("marca") String marca,

            @RequestParam("modelo") String modelo,

            @RequestParam(
                value = "file",
                required = false
            )
            MultipartFile file) {


        try {

            Optional<Vehiculo> optional =
                    vehiculoRepository.findById(id);


            if (optional.isEmpty()) {

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(
                            "Vehiculo no encontrado"
                        );
            }


            Vehiculo vehiculo =
                    optional.get();


            /*
             * Los textos siempre se actualizan.
             */
            vehiculo.setMarca(marca);

            vehiculo.setModelo(modelo);


            /*
             * La fotografía es opcional.
             *
             * Si no llega una fotografía nueva,
             * conservamos la anterior.
             */
            if (file != null
                    && !file.isEmpty()) {

                vehiculo.setMimeType(
                        file.getContentType()
                );

                vehiculo.setFoto(
                        file.getBytes()
                );
            }


            vehiculoRepository.save(vehiculo);


            return ResponseEntity.ok(
                    "Vehiculo actualizado con exito"
            );


        } catch (Exception e) {

            return ResponseEntity
                    .status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                        "Error al actualizar vehiculo: "
                        + e.getMessage()
                    );
        }
    }


    /*
     * DELETE
     * DELETE /auth/vehiculos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarVehiculo(

            @PathVariable Integer id) {


        Optional<Vehiculo> optional =
                vehiculoRepository.findById(id);


        if (optional.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        "Vehiculo no encontrado"
                    );
        }


        vehiculoRepository.delete(
                optional.get()
        );


        return ResponseEntity.ok(
                "Vehiculo eliminado con exito"
        );
    }
}