<template>
  <v-row>
    <v-col md="4" class="mx-auto">


      <v-card class="mx-auto mt-9" align="center">
        <v-card-title>Change password</v-card-title>
        <div class="pa-5">
          <v-text-field
            v-model="password"
            label="Current password"
            :type="showPassword ? 'text' : 'password'"
            :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
            @click:append="showPassword = !showPassword"/>
          <v-text-field
            v-model="passwordR"
            label="Password"
            :rules="passwordRules"
            :counter="20"
            :type="showCurrentPassword ? 'text' : 'password'"
            :append-icon="showCurrentPassword ? 'mdi-eye' : 'mdi-eye-off'"
            @click:append="showCurrentPassword = !showCurrentPassword"/>
          <v-text-field
            label="Repeat password"
            v-model="passwordRConfirm"
            :rules="[passwordConfirmationRule]"
            :type="showCurrentPassword ? 'text' : 'password'"
          />
          <v-btn text
                 @click="change">CHANGE
          </v-btn>
        </div>
      </v-card>
    </v-col>
  </v-row>
</template>

<script>
export default {
  computed: {
    passwordConfirmationRule() {
      return () => (this.passwordR === this.passwordRConfirm) || 'Passwords must match'
    }
  },
  layout: "user_loggedin",
  name: "profile",
  middleware: ["auth"],
  data: () => ({
    showPassword: false,
    showCurrentPassword: false,
    password: "",
    passwordRConfirm: "",
    passwordR: "",
    // TODO reuse password rules between registration and this screen
    passwordRules: [
      p => p.length >= 8 || "Password must consist of at least 8 characters",
      p => p.length <= 20 || "Password must consist of 20 characters at most",
      p => containsUppercase(p) || "Password must contain at least one uppercase character",
      p => containsLowercase(p) || "Password must contain at least one lowercase character",
      p => containsNumber(p) || "Password must contain at least one number",
    ],
  }),
  methods: {
    async change() {

      let data = {
        email: this.$store.$auth.user.email,
        password: this.passwordR,
        passwordConfirm: this.passwordRConfirm,
        currentPassword: this.password
      }
      await this.$store.dispatch("profile/changePassword", data)
      this.passwordR = ""
      this.passwordRConfirm = ""
      this.password = ""
    }
  }
}

function containsNumber(password) {
  for (const c of password) {
    if (c >= '0' && c <= '9') {
      return true
    }
  }
  return false
}

function containsLowercase(password) {
  for (const c of password) {
    if (c.toLowerCase() === c) {
      return true
    }
  }
  return false
}

function containsUppercase(password) {
  for (const c of password) {
    if (c.toUpperCase() === c) {
      return true
    }
  }
  return false
}
</script>

<style scoped>

</style>
