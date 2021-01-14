<template>
  <v-app dark>
    <v-main>
      <v-container>
        <nuxt/>
        <snackbar :snackbars="snackbar.snackbars"/>
      </v-container>
    </v-main>
    <Footer/>
  </v-app>
</template>

<script>
import {mapState} from 'vuex'
import Footer from "@/components/Footer";
import Snackbar from "@/components/Snackbar";

export default {
  components: {Snackbar, Footer},
  computed: {
    ...mapState(["snackbar"])
  },
}
</script>
