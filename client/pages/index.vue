<template>
  <v-form v-model="valid">
    <v-container>
      <h1 class="display-4" align="center">Tic Tac Toe</h1>
      <h3 class="subtitle-1 mt-5" align="center">KIV/PIA semester project</h3>
      <v-row>
        <v-col md="4" class="mx-auto">
          <v-card class="mx-auto mt-9">
            <v-tabs
              :centered="true"
              :grow="true">
              <v-tab>Login</v-tab>
              <v-tab>Register</v-tab>

              <v-tab-item class="pa-5" align="center">
                <span class="subtitle-1 font-weight-light">Already have an account? Sign in!</span>
                <v-text-field
                  v-model="email"
                  :rules="emailRules"
                  label="E-mail"
                  required
                ></v-text-field>

                <v-text-field
                  v-model="password"
                  label="Password"
                  :type="showPassword ? 'text' : 'password'"
                  :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                  @click:append="showPassword = !showPassword"/>

                <v-btn text @click="loginUser" >SIGN IN</v-btn>
              </v-tab-item>

              <v-tab-item class="pa-5" align="center">
                <span class="subtitle-1 font-weight-light">Do not have an account yet? Sign up, it's free!</span>
                <v-text-field
                  v-model="email"
                  :rules="emailRules"
                  label="E-mail"
                  required
                ></v-text-field>

                <v-text-field
                  v-model="username"
                  :rules="nameRules"
                  :counter="16"
                  label="Username"
                  required
                ></v-text-field>
                <v-text-field
                  label="Password"
                  :type="showPassword ? 'text' : 'password'"
                  :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                  @click:append="showPassword = !showPassword"/>
                <v-text-field
                  label="Repeat password"
                  :type="showPassword ? 'text' : 'password'"/>
                <v-btn text >SIGN UP</v-btn>
              </v-tab-item>

            </v-tabs>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </v-form>
</template>

<script>
import Logo from '~/components/Logo.vue'
import VuetifyLogo from '~/components/VuetifyLogo.vue'

export default {
  data: () => ({

    showLogin: true,
    showPassword: false,
    valid: false,
    password: "",
    username: '',
    lastname: '',
    nameRules: [
      v => !!v || 'Username is required',
      v => v.length <= 16 || 'Username must be less than 16 characters',
    ],
    email: '',
    emailRules: [
      v => !!v || 'E-mail is required',
      v => /.+@.+/.test(v) || 'E-mail must be valid',
    ],
  }),
  components: {
    Logo,
    VuetifyLogo
  },
  methods: {
    async loginUser() {
      alert(this.email + this.password);
      try {
        await this.$auth.loginWith("local", {
          data: {
            username: this.email,
            password: this.password
          }
        });
        alert("have logged in :)");
      } catch (e) {
        console.log(e);
        alert("Failed to log in :(");
      }
    }
  }
}
</script>
